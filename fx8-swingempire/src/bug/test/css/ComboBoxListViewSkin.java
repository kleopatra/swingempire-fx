/*
 * Created on 09.08.2017
 *
 */
package test.css;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ComboBoxListViewSkin<T> extends javafx.scene.control.skin.ComboBoxListViewSkin<T>
{
  public ComboBoxListViewSkin(ComboBox<T> comboBox)
  {
    super(comboBox);
  }
  
  @Override
  protected TextField getEditor()
  {
    return super.getEditor();
  }
 }

