package iovi;

public class Statistics {
    int count;
    int rank;

    public int getCount() {
        return count;
    }

    public int getRank() {
        return rank;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Statistics(int count, int rank){
        this.count=count;
        this.rank=rank;
    }
}
