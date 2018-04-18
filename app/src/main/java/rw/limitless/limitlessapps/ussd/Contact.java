package rw.limitless.limitlessapps.ussd;

/**
 * Created by limitlessapps on 05/01/2018.
 */

public class Contact {
    private String ContactImage;
    private String ContactName;
    private String ContactNumber;

    public String getContactImage() {
        return ContactImage;
    }

    public void setContactImage(String contactImage) {
        this.ContactImage = ContactImage;
    }

    public String getContactName() {
        return ContactName;
    }

    public void setContactName(String contactName) {
        ContactName = contactName;
    }

    public String getContactNumber() {
        return ContactNumber;
    }

    public void setContactNumber(String contactNumber) {
        ContactNumber = contactNumber;
    }
}
