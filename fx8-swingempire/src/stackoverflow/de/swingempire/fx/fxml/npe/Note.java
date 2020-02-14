/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

/**
*
* @author blj0011
*/
public class Note
{
   private int Noteid;
   private int mainTableId;
   private String title;
   private String text;

   public Note(int Noteid, int mainTableId, String title, String text)
   {
       this.Noteid = Noteid;
       this.mainTableId = mainTableId;
       this.title = title;
       this.text = text;
   }

   public String getText()
   {
       return text;
   }

   public void setText(String text)
   {
       this.text = text;
   }

   public int getNoteid()
   {
       return Noteid;
   }

   public void setNoteid(int Noteid)
   {
       this.Noteid = Noteid;
   }

   public int getMainTableId()
   {
       return mainTableId;
   }

   public void setMainTableId(int mainTableId)
   {
       this.mainTableId = mainTableId;
   }

   public String getTitle()
   {
       return title;
   }

   public void setTitle(String title)
   {
       this.title = title;
   }

   @Override
   public String toString()
   {
       StringBuilder sb = new StringBuilder();
       sb.append("Note{Noteid=").append(Noteid);
       sb.append(", mainTableId=").append(mainTableId);
       sb.append(", title=").append(title);
       sb.append(", text=").append(text);
       sb.append('}');
       return sb.toString();
   }
}

