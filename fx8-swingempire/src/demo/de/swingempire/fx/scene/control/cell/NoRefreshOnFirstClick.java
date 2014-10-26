/*
 * Created on 22.10.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 * http://stackoverflow.com/a/17867514/203657
 * problem with update - don't see what it is?
 */
@SuppressWarnings("nls")
public final class NoRefreshOnFirstClick extends Application {

   final List< Prospect > _pioneers = new LinkedList<>();
   final List< Prospect > _iss      = new LinkedList<>();
   /* */ Accordion        _accordion;
   /* */ TitledPane       _pioneersTitledPane;
   /* */ TitledPane       _issTitledPane;
   /* */ Prospect         _selected;

   static public final class Prospect {

      final StringProperty          fornameProperty = new SimpleStringProperty();
      final StringProperty          nameProperty    = new SimpleStringProperty();
      final ObjectProperty< Date >  dateProperty    = new SimpleObjectProperty<>();
      final StringProperty          dptProperty     = new SimpleStringProperty();

      public Prospect(String forname, String name, Date date, String dpt) {
         fornameProperty.set(forname);
          nameProperty  .set(name);
          dateProperty  .set(date);
          dptProperty   .set(dpt);
      }

      public StringProperty fornameProperty() {
          return fornameProperty;
      }

      public String getForname() {
          return fornameProperty.get();
      }

      public void setForname(String forname) {
          fornameProperty.set(forname);
      }

      public StringProperty nameProperty() {
          return nameProperty;
      }

      public String getName() {
          return nameProperty.get();
      }

      public void setName(String forname) {
          nameProperty.set(forname);
      }

      public ObjectProperty<Date> dateProperty() {
          return dateProperty;
      }

      public Date getDate() {
          return dateProperty.get();
      }

      public void setDate(Date date) {
          dateProperty.set(date);
      }

      public StringProperty dptProperty() {
          return dptProperty;
      }

      public String getDpt() {
          return dptProperty.get();
      }

      public void setDpt(String dpt) {
          dptProperty.set(dpt);
      }

      @Override
      public boolean equals( Object right ) {
         return right != null && nameProperty.get().equals(((Prospect)right).nameProperty.get());
      }

      @Override
      public int hashCode() {
         return nameProperty.get().hashCode();
      }
  }

   static public final class ProspectTableView extends TableView< Prospect > {

      protected static final SimpleDateFormat _dateFmt = new SimpleDateFormat( "dd/MM/yyyy" );

      public ProspectTableView() {
         final ObservableList< TableColumn< Prospect, ? >> tblColumns = getColumns();
         final TableColumn< Prospect, String  > forname = new TableColumn<>( "Forname" );
         final TableColumn< Prospect, String  > name    = new TableColumn<>( "Name"    );
         final TableColumn< Prospect, Date    > date    = new TableColumn<>( "Date"    );
         final TableColumn< Prospect, Integer > dpt     = new TableColumn<>( "Dpt"     );
         forname.setPrefWidth( 80 );
         name   .setPrefWidth( 80 );
         date   .setPrefWidth( 70 );
         dpt    .setPrefWidth( 50 );
         setPrefWidth( 80+80+70+50+20 );
         forname.setCellValueFactory( new PropertyValueFactory< Prospect, String  >( "forname" ));
         name   .setCellValueFactory( new PropertyValueFactory< Prospect, String  >( "name" ));
         date   .setCellValueFactory( new PropertyValueFactory< Prospect, Date    >( "date" ));
         dpt    .setCellValueFactory( new PropertyValueFactory< Prospect, Integer >( "dpt" ));
         date.setCellFactory(
            new Callback<TableColumn< Prospect, Date >, TableCell< Prospect, Date >>() {
               @Override public TableCell< Prospect, Date > call( TableColumn< Prospect, Date > param) {
                  final TableCell< Prospect, Date > cell = new TableCell< Prospect, Date >() {
                     @Override public void updateItem( final Date item, boolean empty ) {
                        if( item != null ) {
                           setText( _dateFmt.format( item ));
                        }}};
                  return cell;
               }});
         tblColumns.add( forname );
         tblColumns.add( name );
         tblColumns.add( date );
         tblColumns.add( dpt );
         getSortOrder().add(dpt);
         getSortOrder().add(date);
         getSortOrder().add(name);
         setRowFactory(p -> new IdentityCheckingTableRow<>());
      }
      
   }

   public NoRefreshOnFirstClick() {
      final Calendar calendar = Calendar.getInstance();
      calendar.set(1959, Calendar.APRIL, 9);
      final Date APR_9_1959 = calendar.getTime();
      calendar.set(2013, Calendar.MARCH, 28);
      final Date MAR_28_2013 = calendar.getTime();
      _pioneers.add( new Prospect("Alan",     "Shepard",    APR_9_1959,  "Navy"));
      _pioneers.add( new Prospect("John",     "Glenn",      APR_9_1959,  "Navy"));
      _pioneers.add( new Prospect("Gus",      "Grissom",    APR_9_1959,  "Navy"));
      _pioneers.add( new Prospect("Wally",    "Schirra",    APR_9_1959,  "Navy"));
      _pioneers.add( new Prospect("Scott",    "Carpenter",  APR_9_1959,  "Navy"));
      _pioneers.add( new Prospect("Gordon",   "Cooper",     APR_9_1959,  "Navy"));
      _pioneers.add( new Prospect("Deke",     "Slayton",    APR_9_1959,  "Navy"));
      _iss     .add( new Prospect("Pavel",    "Vinogradov", MAR_28_2013, "ISS" ));
      _iss     .add( new Prospect("Chris",    "Cassidy",    MAR_28_2013, "ISS" ));
      _iss     .add( new Prospect("Alexander","Misurkin",   MAR_28_2013, "ISS" ));
      _iss     .add( new Prospect("Fyodor",   "Yurchikhin", MAR_28_2013, "ISS" ));
      _iss     .add( new Prospect("FliLuca",  "Parmitano",  MAR_28_2013, "ISS" ));
      _iss     .add( new Prospect("Karen",    "L. Nyberg",  MAR_28_2013, "ISS" ));
   }

   static void selectProspect( TableView< Prospect > view, Prospect selected ) {
      final int index = view.getItems().indexOf( selected );// see Prospect.equals(Object)
      view.getSelectionModel().select( index );
      view.scrollTo( index );
   }

   void refreshProspectsView( TableView< Prospect > view, List< Prospect > lst ) {
      final ObservableList< Prospect > prospects = view.getItems();
      prospects.clear();
      prospects.addAll( lst );
      if( _selected != null ) {
         selectProspect( view, _selected );
      }
   }

   /**
    * This method get the data from the WEB in a real application...
    * The download is made in a background thread.
    */
   void loadProspects( TitledPane n ) {
      @SuppressWarnings("unchecked")
      final TableView<Prospect> view = (TableView<Prospect>)n.getContent();
      final List< Prospect > source;
      if( n.getText().equals( "Pioneers" )) {
         source = _pioneers;
      }
      else {
         source = _iss;
      }
      refreshProspectsView( view, source );
      view.getColumns().get(0).setVisible(false);
      new Thread(){@Override public void run() {
         try { sleep(750); } catch( final InterruptedException t ){ t.printStackTrace(); }
         Platform.runLater(new Runnable(){@Override public void run(){
             view.getColumns().get(0).setVisible(true);
            }});
      }}.start();
   }

   void expandTitledPaneOf( Prospect prospect ) {
      final TitledPane expanded = _accordion.getExpandedPane();
      final TitledPane pane;
      if( _pioneers.contains( prospect )) {
         pane = _pioneersTitledPane;
      }
      else {
         pane = _issTitledPane;
      }
      if( pane == expanded ) {
         @SuppressWarnings("unchecked")
         final TableView<Prospect> view = (TableView<Prospect>)pane.getContent();
         selectProspect( view, _selected );
      }
      else {
         _accordion.setExpandedPane( pane );
      }
   }

   @Override
   public void start(Stage stage) throws Exception {
      _pioneersTitledPane = new TitledPane( "Pioneers", new ProspectTableView());
      _issTitledPane      = new TitledPane( "ISS"     , new ProspectTableView());
      _accordion          = new Accordion();
      _accordion.getPanes().addAll( _pioneersTitledPane, _issTitledPane );
      _accordion.expandedPaneProperty().addListener( new ChangeListener<TitledPane>() {
         @Override public void changed( ObservableValue<? extends TitledPane> v, TitledPane o, TitledPane n ) {
            if( n != null ) {
               loadProspects( n );
            }
         }} );
      _accordion.setPrefSize( 480, 600 );
      stage.setScene( new Scene( _accordion ));
      stage.centerOnScreen();
      stage.show();
      stage.setOnCloseRequest( new EventHandler<WindowEvent>() {
         @Override public void handle( WindowEvent evt ) { System.exit(0); }});

      final Stage searchResultDialog = new Stage();
      searchResultDialog.setTitle( "Search results" );
      final ProspectTableView tvw = new ProspectTableView();
      tvw.getSelectionModel().getSelectedIndices().addListener(
         new ListChangeListener< Integer >(){
            @Override public void onChanged( final Change< ? extends Integer > evt ) {
               while( evt.next()) {
                  if( ! evt.getList().isEmpty()) {
                     _selected = tvw.getItems().get( evt.getList().get( 0 ));
                     expandTitledPaneOf( _selected );
                  }
               }}});

      tvw.getItems().addAll( _pioneers );
      tvw.getItems().addAll( _iss );
      searchResultDialog.setScene( new Scene( tvw ));
      searchResultDialog.setOnCloseRequest( new EventHandler<WindowEvent>() {
         @Override public void handle( WindowEvent evt ) { System.exit(0); }});
      searchResultDialog.show();
      searchResultDialog.setX( stage.getX() + stage.getWidth());
   }

   public static void main(String[] args) { launch(args); }
}