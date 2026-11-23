using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;

namespace AuroraFileManager;

public partial class MainWindow : Window
{
    private string currentPath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
    private readonly Stack<string> history = new();
    private readonly ObservableCollection<FileRow> rows = new();
    private readonly List<FileRow> allRows = new();
    private List<string> clipboardPaths = new();
    private bool clipboardIsCut;
    private bool showHidden;

    public MainWindow()
    {
        InitializeComponent();
        FilesList.ItemsSource = rows;
        Navigate(currentPath, false);
        PlacesList.SelectedIndex = 0;
    }

    private void Navigate(string path, bool remember = true)
    {
        try
        {
            var full = Path.GetFullPath(path);
            if (!Directory.Exists(full)) return;
            if (remember && !string.Equals(full, currentPath, StringComparison.OrdinalIgnoreCase))
                history.Push(currentPath);
            currentPath = full;
            PathText.Text = full;
            SearchBox.Text = "";
            LoadRows();
        }
        catch (Exception ex) { ShowError("Unable to open this folder", ex); }
    }

    private void LoadRows()
    {
        allRows.Clear();
        rows.Clear();
        try
        {
            var entries = Directory.EnumerateDirectories(currentPath)
                .Concat(Directory.EnumerateFiles(currentPath))
                .OrderBy(p => Directory.Exists(p) ? 0 : 1)
                .ThenBy(Path.GetFileName, StringComparer.CurrentCultureIgnoreCase);

            foreach (var path in entries)
            {
                try
                {
                    if (!showHidden && IsHidden(path)) continue;
                    FileSystemInfo info = Directory.Exists(path)
                        ? new DirectoryInfo(path)
                        : new FileInfo(path);

                    allRows.Add(new FileRow
                    {
                        FullPath = path,
                        Name = Path.GetFileName(path),
                        Type = info is DirectoryInfo ? "File folder" :
                            (string.IsNullOrEmpty(Path.GetExtension(path)) ? "File" :
                            Path.GetExtension(path).TrimStart('.').ToUpperInvariant() + " file"),
                        Size = info is FileInfo file ? FormatSize(file.Length) : "",
                        Modified = info.LastWriteTime.ToString("g"),
                        IsDirectory = info is DirectoryInfo
                    });
                }
                catch { /* Ignore entries whose metadata is unavailable. */ }
            }

            ApplySearch();
            StatusText.Text = $"{rows.Count} items   •   {currentPath}";
        }
        catch (UnauthorizedAccessException)
        {
            StatusText.Text = "Access denied. Open a folder you have permission to browse.";
        }
        catch (Exception ex) { ShowError("Unable to list this folder", ex); }
    }

    private void ApplySearch()
    {
        rows.Clear();
        var query = SearchBox.Text.Trim();
        foreach (var row in allRows)
        {
            if (query.Length == 0 || row.Name.Contains(query, StringComparison.CurrentCultureIgnoreCase))
                rows.Add(row);
        }
        StatusText.Text = $"{rows.Count} of {allRows.Count} items   •   {currentPath}";
    }

    private static bool IsHidden(string path)
    {
        try { return (File.GetAttributes(path) & FileAttributes.Hidden) != 0; }
        catch { return false; }
    }

    private static string FormatSize(long bytes)
    {
        string[] units = ["B", "KB", "MB", "GB", "TB"];
        double size = bytes;
        int i = 0;
        while (size >= 1024 && i < units.Length - 1) { size /= 1024; i++; }
        return $"{size:0.#} {units[i]}";
    }

    private void Back_Click(object sender, RoutedEventArgs e)
    {
        if (history.Count > 0)
        {
            var previous = history.Pop();
            currentPath = previous;
            Navigate(previous, false);
        }
    }

    private void Up_Click(object sender, RoutedEventArgs e)
    {
        var parent = Directory.GetParent(currentPath);
        if (parent != null) Navigate(parent.FullName);
    }

    private void Refresh_Click(object sender, RoutedEventArgs e) => LoadRows();

    private void SearchBox_TextChanged(object sender, TextChangedEventArgs e)
    {
        if (FilesList == null) return;
        ApplySearch();
    }

    private void PlacesList_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        if (PlacesList.SelectedItem is not ListBoxItem item || item.Tag is not string tag) return;
        if (tag == "DRIVES")
        {
            Navigate(Path.GetPathRoot(Environment.GetFolderPath(Environment.SpecialFolder.System)) ?? "C:\\");
            return;
        }
        if (tag == "HOME") { Navigate(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile)); return; }

        var home = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        var folder = tag switch
        {
            "Desktop" => Environment.GetFolderPath(Environment.SpecialFolder.Desktop),
            "MyDocuments" => Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments),
            "Downloads" => Path.Combine(home, "Downloads"),
            "MyPictures" => Environment.GetFolderPath(Environment.SpecialFolder.MyPictures),
            "MyMusic" => Environment.GetFolderPath(Environment.SpecialFolder.MyMusic),
            "MyVideos" => Environment.GetFolderPath(Environment.SpecialFolder.MyVideos),
            _ => home
        };
        if (Directory.Exists(folder)) Navigate(folder);
        else StatusText.Text = $"The {item.Content} folder is not present on this PC.";
    }

    private void FilesList_MouseDoubleClick(object sender, MouseButtonEventArgs e)
    {
        if (FilesList.SelectedItem is not FileRow row) return;
        try
        {
            if (row.IsDirectory) Navigate(row.FullPath);
            else Process.Start(new ProcessStartInfo(row.FullPath) { UseShellExecute = true });
        }
        catch (Exception ex) { ShowError("Unable to open this item", ex); }
    }

    private void NewFolder_Click(object sender, RoutedEventArgs e)
    {
        var name = Prompt("New folder", "Folder name:", "New folder");
        if (string.IsNullOrWhiteSpace(name)) return;
        try { Directory.CreateDirectory(Path.Combine(currentPath, name.Trim())); LoadRows(); }
        catch (Exception ex) { ShowError("Could not create folder", ex); }
    }

    private void NewFile_Click(object sender, RoutedEventArgs e)
    {
        var name = Prompt("New file", "File name:", "New file.txt");
        if (string.IsNullOrWhiteSpace(name)) return;
        try { using (File.Create(Path.Combine(currentPath, name.Trim()))) { } LoadRows(); }
        catch (Exception ex) { ShowError("Could not create file", ex); }
    }

    private void More_Click(object sender, RoutedEventArgs e)
    {
        var menu = new ContextMenu();
        var copy = new MenuItem { Header = "Copy selected" }; copy.Click += (_, _) => CopySelected(false);
        var cut = new MenuItem { Header = "Cut selected" }; cut.Click += (_, _) => CopySelected(true);
        var rename = new MenuItem { Header = "Rename selected" }; rename.Click += (_, _) => RenameSelected();
        var delete = new MenuItem { Header = "Delete selected" }; delete.Click += (_, _) => DeleteSelected();
        var hidden = new MenuItem { Header = showHidden ? "Hide hidden files" : "Show hidden files" };
        hidden.Click += (_, _) => { showHidden = !showHidden; LoadRows(); };
        menu.Items.Add(copy); menu.Items.Add(cut); menu.Items.Add(rename); menu.Items.Add(delete);
        menu.Items.Add(new Separator()); menu.Items.Add(hidden);
        menu.IsOpen = true;
    }

    private void CopySelected(bool cut)
    {
        clipboardPaths = FilesList.SelectedItems.Cast<FileRow>().Select(x => x.FullPath).ToList();
        clipboardIsCut = cut;
        StatusText.Text = $"{clipboardPaths.Count} item(s) {(cut ? "cut" : "copied")}. Browse to a destination and choose Paste.";
    }

    private void Paste_Click(object sender, RoutedEventArgs e)
    {
        var completedCut = new List<string>();
        foreach (var source in clipboardPaths.ToList())
        {
            try
            {
                if (!File.Exists(source) && !Directory.Exists(source))
                    throw new FileNotFoundException("The source item no longer exists.", source);

                var target = Path.Combine(currentPath, Path.GetFileName(source));
                if (string.Equals(Path.GetFullPath(source), Path.GetFullPath(target), StringComparison.OrdinalIgnoreCase))
                    continue;

                if (Directory.Exists(source))
                {
                    if (IsSameOrChild(currentPath, source))
                        throw new IOException("A folder cannot be copied into itself or one of its subfolders.");
                    if (clipboardIsCut) Directory.Move(source, target);
                    else CopyDirectory(source, target);
                }
                else
                {
                    if (clipboardIsCut) File.Move(source, target);
                    else File.Copy(source, target);
                }

                if (clipboardIsCut) completedCut.Add(source);
            }
            catch (Exception ex) { ShowError($"Could not paste {Path.GetFileName(source)}", ex); }
        }

        if (clipboardIsCut)
        {
            clipboardPaths.RemoveAll(path => completedCut.Contains(path, StringComparer.OrdinalIgnoreCase));
            if (clipboardPaths.Count == 0) clipboardIsCut = false;
        }
        LoadRows();
    }

    private static bool IsSameOrChild(string destination, string source)
    {
        var d = Path.GetFullPath(destination).TrimEnd(Path.DirectorySeparatorChar) + Path.DirectorySeparatorChar;
        var s = Path.GetFullPath(source).TrimEnd(Path.DirectorySeparatorChar) + Path.DirectorySeparatorChar;
        return d.StartsWith(s, StringComparison.OrdinalIgnoreCase);
    }

    private static void CopyDirectory(string source, string target)
    {
        Directory.CreateDirectory(target);
        foreach (var file in Directory.EnumerateFiles(source))
        {
            if ((File.GetAttributes(file) & FileAttributes.ReparsePoint) != 0) continue;
            File.Copy(file, Path.Combine(target, Path.GetFileName(file)));
        }
        foreach (var dir in Directory.EnumerateDirectories(source))
        {
            if ((File.GetAttributes(dir) & FileAttributes.ReparsePoint) != 0) continue;
            CopyDirectory(dir, Path.Combine(target, Path.GetFileName(dir)));
        }
    }

    private void RenameSelected()
    {
        if (FilesList.SelectedItems.Count != 1 || FilesList.SelectedItem is not FileRow row)
        {
            MessageBox.Show("Select one item to rename.");
            return;
        }
        var name = Prompt("Rename", "New name:", row.Name);
        if (string.IsNullOrWhiteSpace(name)) return;
        try
        {
            var dest = Path.Combine(currentPath, name.Trim());
            if (row.IsDirectory) Directory.Move(row.FullPath, dest);
            else File.Move(row.FullPath, dest);
            LoadRows();
        }
        catch (Exception ex) { ShowError("Could not rename item", ex); }
    }

    private void DeleteSelected()
    {
        var selected = FilesList.SelectedItems.Cast<FileRow>().ToList();
        if (selected.Count == 0) return;
        if (MessageBox.Show($"Permanently delete {selected.Count} selected item(s)?", "Confirm deletion",
            MessageBoxButton.YesNo, MessageBoxImage.Warning) != MessageBoxResult.Yes) return;

        foreach (var row in selected)
        {
            try
            {
                if (IsProtected(row.FullPath))
                    throw new UnauthorizedAccessException("Aurora blocks deletion of critical Windows system locations.");
                if (row.IsDirectory) Directory.Delete(row.FullPath, true);
                else File.Delete(row.FullPath);
            }
            catch (Exception ex) { ShowError($"Could not delete {row.Name}", ex); }
        }
        LoadRows();
    }

    private static bool IsProtected(string path)
    {
        var full = Path.GetFullPath(path).TrimEnd(Path.DirectorySeparatorChar);
        var windows = Environment.GetFolderPath(Environment.SpecialFolder.Windows).TrimEnd(Path.DirectorySeparatorChar);
        var programFiles = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles).TrimEnd(Path.DirectorySeparatorChar);
        var programFilesX86 = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86).TrimEnd(Path.DirectorySeparatorChar);
        return new[] { windows, programFiles, programFilesX86 }.Any(root =>
            !string.IsNullOrWhiteSpace(root) &&
            (full.Equals(root, StringComparison.OrdinalIgnoreCase) ||
             full.StartsWith(root + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase)))
            || full == Path.GetPathRoot(full)?.TrimEnd(Path.DirectorySeparatorChar);
    }

    private void FilesList_KeyDown(object sender, KeyEventArgs e)
    {
        if (Keyboard.Modifiers == ModifierKeys.Control && e.Key == Key.C) { CopySelected(false); e.Handled = true; }
        else if (Keyboard.Modifiers == ModifierKeys.Control && e.Key == Key.X) { CopySelected(true); e.Handled = true; }
        else if (Keyboard.Modifiers == ModifierKeys.Control && e.Key == Key.V) { Paste_Click(sender, new RoutedEventArgs()); e.Handled = true; }
        else if (e.Key == Key.Delete) { DeleteSelected(); e.Handled = true; }
        else if (e.Key == Key.F5) { LoadRows(); e.Handled = true; }
        else if (e.Key == Key.Back) { Up_Click(sender, new RoutedEventArgs()); e.Handled = true; }
    }

    private static string? Prompt(string title, string label, string initial)
    {
        var dialog = new Window
        {
            Title = title, Width = 390, Height = 155,
            WindowStartupLocation = WindowStartupLocation.CenterOwner,
            ResizeMode = ResizeMode.NoResize, Owner = Application.Current.MainWindow
        };
        var panel = new StackPanel { Margin = new Thickness(14) };
        panel.Children.Add(new TextBlock { Text = label, Margin = new Thickness(0, 0, 0, 8) });
        var input = new TextBox { Text = initial, Margin = new Thickness(0, 0, 0, 10) };
        panel.Children.Add(input);
        var ok = new Button { Content = "OK", IsDefault = true, Width = 80, HorizontalAlignment = HorizontalAlignment.Right };
        ok.Click += (_, _) => dialog.DialogResult = true;
        panel.Children.Add(ok);
        dialog.Content = panel;
        return dialog.ShowDialog() == true ? input.Text : null;
    }

    private static void ShowError(string title, Exception ex) =>
        MessageBox.Show($"{title}.\n\n{ex.Message}", "Aurora File Manager", MessageBoxButton.OK, MessageBoxImage.Error);
}

public class FileRow
{
    public string FullPath { get; set; } = "";
    public string Name { get; set; } = "";
    public string Type { get; set; } = "";
    public string Size { get; set; } = "";
    public string Modified { get; set; } = "";
    public bool IsDirectory { get; set; }
}