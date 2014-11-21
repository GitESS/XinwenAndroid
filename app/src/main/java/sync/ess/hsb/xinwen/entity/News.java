package sync.ess.hsb.xinwen.entity;

/**
 * Created by Hemant Bisht on 11/4/2014.
 */
public class News {

    private String Title;
    private String Description;
    private String Title_id;


    public News(){

    }

    public News(String Title, String Description ,String Title_id){

        this.Title=Title;
        this.Description = Description;
        this.Title_id = Title_id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getTitle_id() {
        return Title_id;
    }

    public void setTitle_id(String title_id) {
        Title_id = title_id;
    }
}
