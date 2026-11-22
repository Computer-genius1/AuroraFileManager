import SwiftUI
import UniformTypeIdentifiers

@main
struct AuroraFileManagerApp: App {
    var body: some Scene {
        WindowGroup {
            BrowserView()
                .preferredColorScheme(nil)
        }
    }
}

private struct FileEntry: Identifiable {
    let url: URL
    let isDirectory: Bool
    var id: String { url.standardizedFileURL.path }
    var name: String { url.lastPathComponent }
}

private enum QuickFilter: String, CaseIterable, Identifiable {
    case all = "All"
    case documents = "Documents"
    case downloads = "Downloads"
    case photos = "Pictures"
    case videos = "Videos"
    case music = "Music"
    var id: String { rawValue }

    func matches(_ entry: FileEntry) -> Bool {
        if self == .all { return true }
        if entry.isDirectory { return false }
        let ext = entry.url.pathExtension.lowercased()
        switch self {
        case .all: return true
        case .documents: return ["pdf", "txt", "rtf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pages", "numbers", "key"].contains(ext)
        case .downloads: return true
        case .photos: return ["jpg", "jpeg", "png", "heic", "gif", "tiff", "webp"].contains(ext)
        case .videos: return ["mp4", "mov", "m4v", "avi", "mkv"].contains(ext)
        case .music: return ["mp3", "m4a", "aac", "wav", "aiff", "flac"].contains(ext)
        }
    }

    var symbol: String {
        switch self {
        case .all: return "folder"
        case .documents: return "doc.text"
        case .downloads: return "arrow.down.circle"
        case .photos: return "photo"
        case .videos: return "video"
        case .music: return "music.note"
        }
    }
}

struct BrowserView: View {
    @State private var currentDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
    @State private var entries: [FileEntry] = []
    @State private var searchText = ""
    @State private var filter: QuickFilter = .all
    @State private var selection = Set<String>()
    @State private var selectionMode = false
    @State private var clipboard: [URL] = []
    @State private var clipboardIsCut = false
    @State private var showingImporter = false
    @State private var showingNewFolder = false
    @State private var showingNewFile = false
    @State private var showingShare = false
    @State private var shareItems: [Any] = []
    @State private var nameInput = ""
    @State private var alertMessage: String?
    @State private var showAlert = false

    private var visibleEntries: [FileEntry] {
        entries.filter { filter.matches($0) }
            .filter { searchText.isEmpty || $0.name.localizedCaseInsensitiveContains(searchText) }
            .sorted {
                if $0.isDirectory != $1.isDirectory { return $0.isDirectory }
                return $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending
            }
    }

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                headerPath
                quickFilters
                if selectionMode { selectionToolbar }
                List {
                    if visibleEntries.isEmpty {
                        VStack(spacing: 10) {
                            Image(systemName: "folder",).font(.system(size: 38)).foregroundColor(.secondary)
                            Text(searchText.isEmpty ? "This folder is empty" : "No matching files")
                                .foregroundColor(.secondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 40)
                        .listRowSeparator(.hidden)
                    }
                    ForEach(visibleEntries) { entry in
                        entryRow(entry)
                    }
                }
                .listStyle(.insetGrouped)
                .searchable(text: $searchText, prompt: "Search files")
            }
            .navigationTitle("Aurora")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(selectionMode ? "Done" : "Select") {
                        selectionMode.toggle()
                        selection.removeAll()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button { showingImporter = true } label: { Label("Import Files", systemImage: "square.and.arrow.down") }
                        Button { showingNewFolder = true; nameInput = "" } label: { Label("New Folder", systemImage: "folder.badge.plus") }
                        Button { showingNewFile = true; nameInput = "" } label: { Label("New Text File", systemImage: "doc.badge.plus") }
                        Button { refresh() } label: { Label("Refresh", systemImage: "arrow.clockwise") }
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .fileImporter(isPresented: $showingImporter, allowedContentTypes: [.item], allowsMultipleSelection: true, onCompletion: importFiles)
            .alert("New Folder", isPresented: $showingNewFolder) {
                TextField("Folder name", text: $nameInput)
                Button("Create", action: createFolder)
                Button("Cancel", role: .cancel) {}
            }
            .alert("New Text File", isPresented: $showingNewFile) {
                TextField("File name", text: $nameInput)
                Button("Create", action: createTextFile)
                Button("Cancel", role: .cancel) {}
            }
            .alert("Aurora", isPresented: $showAlert) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(alertMessage ?? "")
            }
            .sheet(isPresented: $showingShare) {
                ActivityView(activityItems: shareItems)
            }
            .onAppear(perform: refresh)
        }
        .navigationViewStyle(.stack)
    }

    private var headerPath: some View {
        HStack(spacing: 6) {
            Image(systemName: "lock.shield").foregroundColor(.green)
            Text("On My iPhone • Aurora Documents")
                .font(.caption)
                .foregroundColor(.secondary)
            Spacer()
            Text("\(entries.count) items")
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .padding(.horizontal)
        .padding(.vertical, 9)
    }

    private var quickFilters: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(QuickFilter.allCases) { item in
                    Button {
                        filter = item
                    } label: {
                        Label(item.rawValue, systemImage: item.symbol)
                            .font(.caption.weight(.medium))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .background(filter == item ? Color.accentColor.opacity(0.16) : Color(.secondarySystemBackground))
                            .foregroundColor(filter == item ? .accentColor : .primary)
                            .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal)
            .padding(.bottom, 8)
        }
    }

    private var selectionToolbar: some View {
        HStack {
            Text("\(selection.count) selected").font(.caption).foregroundColor(.secondary)
            Spacer()
            Button("Select All") { selection = Set(visibleEntries.map(\.id)) }
            Button("Copy") { copySelection(cut: false) }.disabled(selection.isEmpty)
            Button("Cut") { copySelection(cut: true) }.disabled(selection.isEmpty)
            Button("Delete", role: .destructive) { deleteSelection() }.disabled(selection.isEmpty)
        }
        .font(.caption.weight(.semibold))
        .padding(.horizontal)
        .padding(.vertical, 10)
    }

    private func entryRow(_ entry: FileEntry) -> some View {
        Button {
            if selectionMode {
                if selection.contains(entry.id) { selection.remove(entry.id) }
                else { selection.insert(entry.id) }
            } else if entry.isDirectory {
                currentDirectory = entry.url
                searchText = ""
                filter = .all
                refresh()
            } else {
                shareItems = [entry.url]
                showingShare = true
            }
        } label: {
            HStack(spacing: 12) {
                Image(systemName: icon(for: entry))
                    .font(.system(size: 22))
                    .foregroundColor(entry.isDirectory ? .accentColor : .secondary)
                    .frame(width: 30)
                VStack(alignment: .leading, spacing: 3) {
                    Text(entry.name).foregroundColor(.primary).lineLimit(1)
                    Text(entry.isDirectory ? "Folder" : fileSize(entry.url))
                        .font(.caption2).foregroundColor(.secondary)
                }
                Spacer()
                if selectionMode {
                    Image(systemName: selection.contains(entry.id) ? "checkmark.circle.fill" : "circle")
                        .foregroundColor(selection.contains(entry.id) ? .accentColor : .secondary)
                } else if entry.isDirectory {
                    Image(systemName: "chevron.right").font(.caption).foregroundColor(.tertiary)
                } else {
                    Image(systemName: "square.and.arrow.up").font(.caption).foregroundColor(.secondary)
                }
            }
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .contextMenu {
            Button { rename(entry) } label: { Label("Rename", systemImage: "pencil") }
            Button { shareItems = [entry.url]; showingShare = true } label: { Label("Share", systemImage: "square.and.arrow.up") }
            Button(role: .destructive) { remove(entry.url) } label: { Label("Delete", systemImage: "trash") }
        }
    }

    private func refresh() {
        do {
            let urls = try FileManager.default.contentsOfDirectory(at: currentDirectory, includingPropertiesForKeys: [.isDirectoryKey, .fileSizeKey], options: [.skipsHiddenFiles])
            entries = urls.map { url in
                let directory = (try? url.resourceValues(forKeys: [.isDirectoryKey]).isDirectory) ?? false
                return FileEntry(url: url, isDirectory: directory)
            }
        } catch {
            report("Couldn't read this folder: \(error.localizedDescription)")
        }
    }

    private func importFiles(_ result: Result<[URL], Error>) {
        switch result {
        case .success(let urls):
            for url in urls {
                let access = url.startAccessingSecurityScopedResource()
                defer { if access { url.stopAccessingSecurityScopedResource() } }
                let destination = currentDirectory.appendingPathComponent(url.lastPathComponent)
                do {
                    if FileManager.default.fileExists(atPath: destination.path) {
                        report("\(url.lastPathComponent) already exists.")
                        continue
                    }
                    try FileManager.default.copyItem(at: url, to: destination)
                } catch {
                    report("Import failed: \(error.localizedDescription)")
                }
            }
            refresh()
        case .failure(let error):
            report("Import failed: \(error.localizedDescription)")
        }
    }

    private func createFolder() {
        guard validName(nameInput) else { report("Enter a valid folder name."); return }
        do {
            try FileManager.default.createDirectory(at: currentDirectory.appendingPathComponent(nameInput), withIntermediateDirectories: false)
            refresh()
        } catch { report(error.localizedDescription) }
    }

    private func createTextFile() {
        var filename = nameInput.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !filename.isEmpty else { filename = "Untitled File.txt"; nameInput = filename; return }
        if URL(fileURLWithPath: filename).pathExtension.isEmpty { filename += ".txt" }
        do {
            let url = currentDirectory.appendingPathComponent(filename)
            guard !FileManager.default.fileExists(atPath: url.path) else { report("A file with that name already exists."); return }
            try Data().write(to: url, options: .completeFileProtection)
            refresh()
        } catch { report(error.localizedDescription) }
    }

    private func copySelection(cut: Bool) {
        clipboard = visibleEntries.filter { selection.contains($0.id) }.map(\.url)
        clipboardIsCut = cut
        selection.removeAll()
        report(cut ? "Items ready to move. Navigate to a destination and use Paste." : "Items copied. Navigate to a destination and use Paste.")
    }

    private func pasteClipboard() {
        for source in clipboard {
            let destination = currentDirectory.appendingPathComponent(source.lastPathComponent)
            do {
                if FileManager.default.fileExists(atPath: destination.path) { throw CocoaError(.fileWriteFileExists) }
                if clipboardIsCut { try FileManager.default.moveItem(at: source, to: destination) }
                else { try FileManager.default.copyItem(at: source, to: destination) }
            } catch { report("Paste failed: \(error.localizedDescription)") }
        }
        clipboard.removeAll()
        refresh()
    }

    private func deleteSelection() {
        let urls = entries.filter { selection.contains($0.id) }.map(\.url)
        for url in urls { remove(url, showResult: false) }
        selection.removeAll()
        refresh()
    }

    private func remove(_ url: URL, showResult: Bool = true) {
        do {
            try FileManager.default.removeItem(at: url)
            if showResult { refresh() }
        } catch { report("Delete failed: \(error.localizedDescription)") }
    }

    private func rename(_ entry: FileEntry) {
        let alert = UIAlertController(title: "Rename", message: nil, preferredStyle: .alert)
        alert.addTextField { $0.text = entry.name }
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        alert.addAction(UIAlertAction(title: "Rename", style: .default) { _ in
            guard let newName = alert.textFields?.first?.text?.trimmingCharacters(in: .whitespacesAndNewlines), validName(newName) else { return }
            let destination = entry.url.deletingLastPathComponent().appendingPathComponent(newName)
            do { try FileManager.default.moveItem(at: entry.url, to: destination); refresh() }
            catch { report(error.localizedDescription) }
        })
        UIApplication.shared.topViewController?.present(alert, animated: true)
    }

    private func validName(_ name: String) -> Bool {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        return !trimmed.isEmpty && trimmed != "." && trimmed != ".." && !trimmed.contains("/")
    }

    private func icon(for entry: FileEntry) -> String {
        if entry.isDirectory { return "folder.fill" }
        switch entry.url.pathExtension.lowercased() {
        case "pdf": return "doc.richtext"
        case "jpg", "jpeg", "png", "heic", "gif": return "photo"
        case "mp4", "mov", "m4v": return "video"
        case "mp3", "m4a", "wav": return "music.note"
        case "zip", "7z", "rar": return "archivebox"
        default: return "doc"
        }
    }

    private func fileSize(_ url: URL) -> String {
        let bytes = (try? url.resourceValues(forKeys: [.fileSizeKey]).fileSize) ?? 0
        let formatter = ByteCountFormatter()
        formatter.countStyle = .file
        return formatter.string(fromByteCount: Int64(bytes))
    }

    private func report(_ message: String) {
        alertMessage = message
        showAlert = true
    }
}

private struct ActivityView: UIViewControllerRepresentable {
    let activityItems: [Any]
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }
    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}

private extension UIApplication {
    var topViewController: UIViewController? {
        connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }?
            .rootViewController?
            .topPresented
    }
}

private extension UIViewController {
    var topPresented: UIViewController {
        presentedViewController?.topPresented ?? self
    }
}
