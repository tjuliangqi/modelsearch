package cn.tju.modelsearch.domain;

public class ModelSql {
    private String ID;
    private String author;
    private String subclassName;
    private String className;
    private String size;
    private String imgPath;
    private String filePath;
    private int downloadTimes;

    public int getDownloadTimes() {
        return downloadTimes;
    }

    public void setDownloadTimes(int downloadTimes) {
        this.downloadTimes = downloadTimes;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSubclassName() {
        return subclassName;
    }

    public void setSubclassName(String subclassName) {
        this.subclassName = subclassName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "ModelSql{" +
                "ID='" + ID + '\'' +
                ", author='" + author + '\'' +
                ", subclassName='" + subclassName + '\'' +
                ", className='" + className + '\'' +
                ", size='" + size + '\'' +
                ", imgPath='" + imgPath + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
