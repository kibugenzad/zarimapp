package rw.limitless.limitlessapps.ussd;

/**
 * Created by limitlessapps on 13/01/2018.
 */

public class Service_item {

    public Service_item() {
    }

    public Service_item(String title, String service_owner, String id,String ussd_code, String user_key) {
        this.title = title;
        this.service_owner = service_owner;
        this.ussd_code = ussd_code;
        this.id = id;
        this.user_key = user_key;
    }
    private String title, service_owner, ussd_code, id,user_key;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getService_owner() {
        return service_owner;
    }

    public void setService_owner(String service_owner) {
        this.service_owner = service_owner;
    }

    public String getUssd_code() {
        return ussd_code;
    }

    public void setUssd_code(String ussd_code) {
        this.ussd_code = ussd_code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String id) {
        this.user_key = user_key;
    }

}
