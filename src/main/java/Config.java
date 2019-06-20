public class Config {
    public int getConnectionPoolMin() {
        return 5;  // 2
    }

    public int getConnectionPoolMax() {
        return 20; // 5
    }

    public int getConnectTimeout() {
        return 30;
    }

    public int getReadTimeout() {
        return 120;
    }
}
