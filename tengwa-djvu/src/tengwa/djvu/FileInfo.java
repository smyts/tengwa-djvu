package tengwa.djvu;

public class FileInfo {
    public String filePath;
    public int pageTotal;

    public FileInfo() { }

    public FileInfo(String path, int pageCount) {
        this.filePath = path;
        this.pageTotal = pageCount;
    }
}
