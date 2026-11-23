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
    private int loadGeneration;

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
            if (!Directory.Exists(full))
            {
                StatusText.Text = "This folder is no longer available.";
                return;
            }
            if (remember && !string.Equals(full, currentPath, StringComparison.OrdinalIgnoreCase))
                history.Push(currentPath);
            currentPath = full;
            PathText.Text = full;
            SearchBox.Text = "";
            _ = LoadRowsAsync();
        }
        catch (Exception ex) { ShowError("Unable to open this folder", ex); }
    }

    private async Task LoadRowsAsync()
    {
        var path = currentPath;
        var generation = Interlocked.Increment(ref loadGeneration);
        StatusText.Text = "Loading folder…";
        try
        {
            var loaded = await Task.Run(() =>
            {
                var result = new List<FileRow>();
                var entries = Directory.EnumerateFileSystemEntries(path)
                    .Select(entry => new { Path = entry, IsDirectory = IsDirectoryEntry(entry) })
                    .OrderBy(entry => entry.IsDirectory ? 0 : 1)
                    .ThenBy(entry => Path.GetFileName(entry.Path), StringComparer.CurrentCultureIgnoreCase);

                foreach (var entry in entries)
                {
                    try
                    {
                        if (!showHidden && IsHidden(entry.Path)) continue;
                        FileSystemInfo info = entry.IsDirectory
                            ? new DirectoryInfo(entry.Path)
                            : new FileInfo(entry.Path);
                        info.Refresh();
                        result.Add(new FileRow
                        {
                            FullPath = entry.Path,
                            Name = Path.GetFileName(entry.Path),
                            Type = entry.IsDirectory ? "File folder" :
                                (string.IsNullOrEmpty(Path.GetExtension(entry.Path)) ? "File" :
                                Path.GetExtension(entry.Path).TrimStart('.').ToUpperInvariant() + " file"),
                            Size = info is FileInfo file ? FormatSize(file.Length) : "",
                            Modified = info.LastWriteTime.ToString("g"),
                            IsDirectory = entry.IsDirectory
                        });
                    }
                    catch { /* Ignore entries whose metadata cannot be read. */ }
                }
                return result;
            });

            if (generation != loadGeneration || !string.Equals(path, currentPath, StringComparison.OrdinalIgnoreCase))
                return;

            allRows.Clear();
            allRows.AddRange(loaded);
            ApplySearch();
        }
        catch (UnauthorizedAccessException)
        {
            if (generation != loadGeneration) return;
            allRows.Clear();
            rows.Clear();
            StatusText.Text = "Access denied. Open a folder you have permission to browse.";
        }
        catch (Exception ex)
        {
            if (generation != loadGeneration) return;
            allRows.Clear();
            rows.Clear();
            ShowError("Unable to list this folder", ex);
        }
    }

    private static bool IsDirectoryEntry(string path)
    {
        try { return (File.GetAttributes(path) & FileAttributes.Directory) != 0; }
        catch { return false; }
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
        while (history.Count > 0)
        {
            var previous = history.Pop();
            if (Directory.Exists(previous)) { Navigate(previous, false); return; }
        }
        StatusText.Text = "There are no previous folders to visit.";
    }

    private void Up_Click(object sender, RoutedEventArgs e)
    {
        var parent = Directory.GetParent(currentPath);
        if (parent != null) Navigate(parent.FullName);
    }

    private void Refresh_Click(object sender, RoutedEventArgs e) => _ = LoadRowsAsync();

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
        if (!TryGetSafeName(name, out var safeName)) return;
        try { Directory.CreateDirectory(Path.Combine(currentPath, safeName)); _ = LoadRowsAsync(); }
        catch (Exception ex) { ShowError("Could not create folder", ex); }
    }

    private void NewFile_Click(object sender, RoutedEventArgs e)
    {
        var name = Prompt("New file", "File name:", "New file.txt");
        if (!TryGetSafeName(name, out var safeName)) return;
        try
        {
            var path = Path.Combine(currentPath, safeName);
            using (File.Create(path)) { }
            _ = LoadRowsAsync();
        }
        catch (Exception ex) { ShowError("Could not create file", ex); }
    }

    private static bool TryGetSafeName(string? input, out string name)
    {
        name = input?.Trim() ?? "";
        if (string.IsNullOrWhiteSpace(name))
        {
            MessageBox.Show("Enter a file or folder name.", "Invalid name", MessageBoxButton.OK, MessageBoxImage.Warning);
            return false;
        }
        if (name is "." or ".." || name.EndsWith('.') || name.EndsWith(' ') ||
            name.IndexOfAny(Path.GetInvalidFileNameChars()) >= 0 ||
            name.IndexOfAny(['<', '>', ':', '"', '/', '\\', '|', '?', '*']) >= 0)
        {
            MessageBox.Show("This name contains characters or formatting that Windows does not allow.", "Invalid name",
                MessageBoxButton.OK, MessageBoxImage.Warning);
            return false;
        }
        var stem = name.Split('.')[0].TrimEnd(' ').ToUpperInvariant();
        if (new[] { "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                    "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9" }.Contains(stem))
        {
            MessageBox.Show("Windows reserves that name. Choose a different name.", "Invalid name",
                MessageBoxButton.OK, MessageBoxImage.Warning);
            return false;
        }
        return true;
    }

    private void More_Click(object sender, RoutedEventArgs e)
    {
        var menu = new ContextMenu();
        var properties = new MenuItem { Header = "Properties" };
        properties.Click += (_, _) => ShowProperties();
        var copy = new MenuItem { Header = "Copy selected" }; copy.Click += (_, _) => CopySelected(false);
        var cut = new MenuItem { Header = "Cut selected" }; cut.Click += (_, _) => CopySelected(true);
        var rename = new MenuItem { Header = "Rename selected" }; rename.Click += (_, _) => RenameSelected();
        var delete = new MenuItem { Header = "Delete selected" }; delete.Click += (_, _) => DeleteSelected();
        var hidden = new MenuItem { Header = showHidden ? "Hide hidden files" : "Show hidden files" };
        hidden.Click += (_, _) => { showHidden = !showHidden; _ = LoadRowsAsync(); };
        properties.IsEnabled = FilesList.SelectedItems.Count == 1;
        menu.Items.Add(properties);
        menu.Items.Add(new Separator());
        menu.Items.Add(copy); menu.Items.Add(cut); menu.Items.Add(rename); menu.Items.Add(delete);
        menu.Items.Add(new Separator()); menu.Items.Add(hidden);
        menu.IsOpen = true;
    }

    private void ShowProperties()
    {
        if (FilesList.SelectedItem is not FileRow row) return;
        try
        {
            FileSystemInfo info = row.IsDirectory ? new DirectoryInfo(row.FullPath) : new FileInfo(row.FullPath);
            info.Refresh();
            var attributes = info.Attributes.ToString();
            var size = info is FileInfo file ? FormatSize(file.Length) + $" ({file.Length:N0} bytes)" : "Folder";
            var details = $"Name: {info.Name}\nType: {row.Type}\nLocation: {info.FullName}\nSize: {size}\nCreated: {info.CreationTime}\nModified: {info.LastWriteTime}\nAccessed: {info.LastAccessTime}\nAttributes: {attributes}";
            MessageBox.Show(details, "Properties — " + info.Name, MessageBoxButton.OK, MessageBoxImage.Information);
        }
        catch (Exception ex) { ShowError("Unable to read item properties", ex); }
    }

    private void CopySelected(bool cut)
    {
        clipboardPaths = FilesList.SelectedItems.Cast<FileRow>().Select(x => x.FullPath).ToList();
        clipboardIsCut = cut;
        StatusText.Text = clipboardPaths.Count == 0
            ? "Select one or more items first."
            : $"{clipboardPaths.Count} item(s) {(cut ? "cut" : "copied")} — browse to a destination and select Paste.";
    }

    private void Paste_Click(object sender, RoutedEventArgs e)
    {
        if (clipboardPaths.Count == 0)
        {
            StatusText.Text = "Nothing to paste. Select items and choose Copy or Cut first.";
            return;
        }

        var completedCut = new List<string>();
        var copiedCount = 0;
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
                    if (Directory.Exists(target))
                    {
                        MessageBox.Show($"A folder named '{Path.GetFileName(target)}' already exists. Merge/replace is not supported; choose another destination.",
                            "Destination already exists", MessageBoxButton.OK, MessageBoxImage.Information);
                        continue;
                    }
                    if (File.Exists(target))
                    {
                        MessageBox.Show($"A file named '{Path.GetFileName(target)}' already exists.", "Destination already exists",
                            MessageBoxButton.OK, MessageBoxImage.Warning);
                        continue;
                    }
                    if (clipboardIsCut) Directory.Move(source, target);
                    else CopyDirectory(source, target);
                }
                else
                {
                    if (Directory.Exists(target))
                    {
                        MessageBox.Show($"A folder named '{Path.GetFileName(target)}' already exists.", "Destination already exists",
                            MessageBoxButton.OK, MessageBoxImage.Warning);
                        continue;
                    }
                    var overwrite = File.Exists(target);
                    if (overwrite && MessageBox.Show($"'{Path.GetFileName(target)}' already exists. Replace it?",
                        "Confirm overwrite", MessageBoxButton.YesNo, MessageBoxImage.Warning) != MessageBoxResult.Yes)
                        continue;

                    if (clipboardIsCut) File.Move(source, target, overwrite);
                    else File.Copy(source, target, overwrite);
                }

                copiedCount++;
                if (clipboardIsCut) completedCut.Add(source);
            }
            catch (Exception ex) { ShowError($"Could not paste {Path.GetFileName(source)}", ex); }
        }

        if (clipboardIsCut)
        {
            clipboardPaths.RemoveAll(path => completedCut.Contains(path, StringComparer.OrdinalIgnoreCase));
            if (clipboardPaths.Count == 0) clipboardIsCut = false;
        }
        StatusText.Text = $"{copiedCount} item(s) pasted. {clipboardPaths.Count} item(s) remain in the clipboard.";
        _ = LoadRowsAsync();
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
        foreach (var entry in Directory.EnumerateFileSystemEntries(source))
        {
            var attributes = File.GetAttributes(entry);
            if ((attributes & FileAttributes.ReparsePoint) != 0) continue;
            var destination = Path.Combine(target, Path.GetFileName(entry));
            if ((attributes & FileAttributes.Directory) != 0)
                CopyDirectory(entry, destination);
            else
                File.Copy(entry, destination, false);
        }
    }

    private void RenameSelected()
    {
        if (FilesList.SelectedItems.Count != 1 || FilesList.SelectedItem is not FileRow row)
        {
            MessageBox.Show("Select one item to rename.");
            return;
        }
        var input = Prompt("Rename", "New name:", row.Name);
        if (!TryGetSafeName(input, out var name)) return;
        try
        {
            var dest = Path.Combine(currentPath, name);
            if (File.Exists(dest) || Directory.Exists(dest))
                throw new IOException("An item with that name already exists.");
            if (row.IsDirectory) Directory.Move(row.FullPath, dest);
            else File.Move(row.FullPath, dest);
            _ = LoadRowsAsync();
        }
        catch (Exception ex) { ShowError("Could not rename item", ex); }
    }

    private void DeleteSelected()
    {
        var selected = FilesList.SelectedItems.Cast<FileRow>().ToList();
        if (selected.Count == 0) return;
        if (MessageBox.Show($"Permanently delete {selected.Count} selected item(s)? This cannot be undone.",
            "Confirm permanent deletion", MessageBoxButton.YesNo, MessageBoxImage.Warning) != MessageBoxResult.Yes) return;

        foreach (var row in selected)
        {
            try
            {
                if (IsProtected(row.FullPath))
                    throw new UnauthorizedAccessException("Aurora blocks deletion of critical Windows system locations.");
                SafeDelete(row.FullPath);
            }
            catch (Exception ex) { ShowError($"Could not delete {row.Name}", ex); }
        }
        _ = LoadRowsAsync();
    }

    private static void SafeDelete(string path)
    {
        var attributes = File.GetAttributes(path);
        if ((attributes & FileAttributes.ReparsePoint) != 0)
        {
            if ((attributes & FileAttributes.Directory) != 0) Directory.Delete(path, false);
            else File.Delete(path);
            return;
        }

        if ((attributes & FileAttributes.Directory) == 0)
        {
            File.Delete(path);
            return;
        }

        foreach (var child in Directory.EnumerateFileSystemEntries(path))
            SafeDelete(child);
        Directory.Delete(path, false);
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
        else if (e.Key == Key.F5) { Refresh_Click(sender, new RoutedEventArgs()); e.Handled = true; }
        else if (e.Key == Key.Back) { Up_Click(sender, new RoutedEventArgs()); e.Handled = true; }
        else if (e.Key == Key.Enter && FilesList.SelectedItem is FileRow row)
        {
            if (row.IsDirectory) Navigate(row.FullPath);
            else FilesList_MouseDoubleClick(sender, new MouseButtonEventArgs(Mouse.PrimaryDevice, 0, MouseButton.Left));
            e.Handled = true;
        }
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
        dialog.Loaded += (_, _) => { input.Focus(); input.SelectAll(); };
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