package iovi;

public class Statistics {
    int count;
    int rank;
    String originalLink;
    String key;

    public int getCount() {
        return count;
    }

    public int getRank() {
        return rank;
    }

    public String getKey() {
        return key;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Statistics(String originalLink, String key, int count,int rank){
        this.count=count;
        this.rank=rank;
        this.originalLink=originalLink;
        this.key=key;
    }
}
