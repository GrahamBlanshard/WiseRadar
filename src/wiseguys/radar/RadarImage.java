package wiseguys.radar;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Graham on 8/23/2017.
 */
public class RadarImage {
    /* JSON Response EX:
    {
        "src":"\/data\/radar\/temp_image\/\/XBE\/XBE_PRECIP_RAIN_2017_08_24_00_00.GIF",
        "src_detailed":"\/data\/radar\/detailed\/temp_image\/\/XBE\/XBE_PRECIP_RAIN_2017_08_24_00_00.GIF",
        "timestring":"2017-08-23, 06:00 PM CST",
        "timestamp":1503532800
    },
    */
    @SerializedName("src")
    @Expose
    private String src;
    @SerializedName("src_detailed")
    @Expose
    private String srcDetailed;
    @SerializedName("timestring")
    @Expose
    private String timestring;
    @SerializedName("timestamp")
    @Expose
    private Integer timestamp;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrcDetailed() {
        return srcDetailed;
    }

    public void setSrcDetailed(String srcDetailed) {
        this.srcDetailed = srcDetailed;
    }

    public String getTimestring() {
        return timestring;
    }

    public void setTimestring(String timestring) {
        this.timestring = timestring;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }
}
