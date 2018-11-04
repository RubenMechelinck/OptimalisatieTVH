public class Machine_Type {

    private int volume;
    private int serviceTime;
    private String name;

    public Machine_Type(int volume, int serviceTime, String name) {
        this.volume = volume;
        this.serviceTime = serviceTime;
        this.name = name;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void print(){
        System.out.println("volume: " + volume + " servicetime: " + serviceTime + " name: " + name);
    }
}
