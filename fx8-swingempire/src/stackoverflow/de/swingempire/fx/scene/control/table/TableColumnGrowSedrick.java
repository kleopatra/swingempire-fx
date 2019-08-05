/*
 * Created on 31.07.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.JTable;

import com.sun.javafx.scene.control.TableColumnBaseHelper;

import de.swingempire.testfx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.ResizeFeatures;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * https://stackoverflow.com/q/57245216/203657
 * resize one column to fill all available width
 * 
 * This is the answer of Sedrick: binds prefWidth of the "filling" column
 * to available space (table width - sum of others)
 * drawback: filling column must not be resizable
 * 
 * Trying with simple resizePolicy
 * 
 * ----
 * 
 * unrelated: horizontal scrollbar flickers on/off with constrainedResize
 * https://bugs.openjdk.java.net/browse/JDK-8089009
 * reported 2014
 *
 * there are more open bugs, see scribbles on tableView
 * -------
 * 
 * unrelated: initial sizing doesn't honor prefWidth
 * https://bugs.openjdk.java.net/browse/JDK-8157687
 * reported 2016
 * 
 * analysed by reported to be caused by field isFirstRun: should be false
 * to force prefWidth honoring (can be seen here to be true)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableColumnGrowSedrick extends Application{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    public static final Callback<ResizeFeatures, Boolean> MY_CONSTRAINED_RESIZE_POLICY = new Callback<ResizeFeatures, Boolean>() {

        private boolean isFirstRun = true;

        @Override public String toString() {
            return "constrained-resize";
        }

        @Override public Boolean call(ResizeFeatures prop) {
           // JTable table;
            TableColumn<?, ?> column = prop.getColumn();
            String text = column != null ? column.getText() : "table resize";
            String width = column != null ? " width/ min/pref/max/: " 
                    + column.getWidth()  
                    + "/" + column.getMinWidth()
                    + "/" + column.getPrefWidth()
                     + "/" + column.getMaxWidth()
                    
                    :   "";
//            LOG.info("resizeFeature: " + text + width + " / delta" + prop.getDelta());
            TableView<?> table = prop.getTable();
            List<? extends TableColumnBase<?,?>> visibleLeafColumns = table.getVisibleLeafColumns();
            Boolean result = TableColumnResizeHelper.constrainedResize(prop,
                     // providing an unconditional false leads to correct initial sizing
                     // honoring prefWidth
                                               false,
                                               getContentWidth(table),
                                               visibleLeafColumns);
            isFirstRun = ! isFirstRun ? false : ! result;
            return result;
        }
        
        private double getContentWidth(TableView<?> table) {
            return  (double) FXUtils.invokeGetFieldValue(TableView.class, table, "contentWidth");
        }

    };

    public static final Callback<ResizeFeatures, Boolean> MY_RESIZE_POLICY = new Callback<ResizeFeatures, Boolean>() {
        @Override public String toString() {
            return "unconstrained-resize";
        }

        @Override public Boolean call(ResizeFeatures prop) {
            TableColumn<?, ?> column = prop.getColumn();
            String text = column != null ? column.getText() : "table resize";
            String width = column != null ? " width/ min/pref/max/: " 
                    + column.getWidth()  
                    + "/" + column.getMinWidth()
                    + "/" + column.getPrefWidth()
                     + "/" + column.getMaxWidth()
                    
                    :   "";
//            LOG.info("resizeFeature: " + text + width + " / delta" + prop.getDelta());
            
            if (column != null) {
                // hard code to first column
                TableColumn<?, ?> fillingColumn = getFillingColumn(prop.getTable());
                boolean isFillingColumn = fillingColumn == column;
                boolean isShrinking = prop.getDelta() < 0;
                double contentWidth = getContentWidth(prop.getTable());
                // resizing the filling column, need to inc/dec widths of siblings
                if (isFillingColumn) {
                    List<TableColumn<?, ?>> siblings = getSiblings(prop.getTable(), column);
                    double perColumn =  prop.getDelta() / siblings.size();
                    LOG.info("per column: " + perColumn);
                    // doesn't work, small deltas ...
                    siblings.forEach(c -> TableColumnBaseHelper.setWidth(c, c.getWidth() - perColumn));
                    TableColumnBaseHelper.setWidth(column, column.getWidth() + prop.getDelta() - 0.5);
                } else {
                    
                }
//                double targetWidth = column.getWidth() + prop.getDelta();
//                TableColumnBaseHelper.setWidth(column, targetWidth);
            } else {
                if (prop.getTable().getColumns().isEmpty()) return true;
                TableColumn<?, ?> fillingColumn = getFillingColumn(prop.getTable());
                boolean isShrinking = prop.getDelta() < 0;
                double contentWidth = (double) FXUtils.invokeGetFieldValue(TableView.class, prop.getTable(), "contentWidth");
                double columnWidth = 0;
                for (int i = 0; i < prop.getTable().getColumns().size(); i++) {
                    columnWidth += ((TableColumn) prop.getTable().getColumns().get(i)).getWidth();
                }
                // magic delta to keep hbar from showing up
                double delta = contentWidth - columnWidth - .5;
                double targetWidth = fillingColumn.getWidth() + delta;
                TableColumnBaseHelper.setWidth(fillingColumn, targetWidth);
                
                
            }
            return true; //TableView.UNCONSTRAINED_RESIZE_POLICY.call(prop);
//            double result = TableUtil.resize(prop.getColumn(), prop.getDelta());
//            return Double.compare(result, 0.0) == 0;
        }
        
        private List<TableColumn<?, ?>> getSiblings(TableView<?> table, TableColumn<?, ?> sibling) {
            List visible = table.getVisibleLeafColumns().stream()
                    .filter(c -> c != sibling)
                    .collect(Collectors.toList());
            return visible;
        }
        
        private TableColumn<?, ?> getFillingColumn(TableView<?> table) {
            return table.getVisibleLeafColumn(0);
        }
        
        private double getColumnWidths(TableView<?> table) {
            double width = 0;
            for(TableColumn<?, ?> column : table.getVisibleLeafColumns()) {
                width += column.getWidth();
            }
            return width;
        }
        
        private double getContentWidth(TableView<?> table) {
            return  (double) FXUtils.invokeGetFieldValue(TableView.class, table, "contentWidth");
        }
    };

    @Override
    public void start(Stage primaryStage) {

        // Simple Interface
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        TableView<Person> tableView = new TableView<>();
        TableColumn<Person, String> colFirstName = new TableColumn<>("First Name");
        TableColumn<Person, String> colMiddleName = new TableColumn<>("Last Name");
        TableColumn<Person, String> colLastName = new TableColumn<>("Last Name");

        colFirstName.setCellValueFactory(tf -> tf.getValue().firstNameProperty());
        colMiddleName.setCellValueFactory(tf -> tf.getValue().middleNameProperty());
        colLastName.setCellValueFactory(tf -> tf.getValue().lastNameProperty());

        colFirstName.setPrefWidth(160);
        tableView.getColumns().addAll(colFirstName, colMiddleName, colLastName);

        tableView.getItems().addAll(
                new Person("Martin", "One", "Brody"),
                new Person("Matt", "Two", "Hooper"),
                new Person("Sam", "Three", "Quint")
        );


        root.getChildren().add(tableView);

        // Show the stage
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("TableColumnGrow Sample");
        primaryStage.show();

//        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setColumnResizePolicy(MY_CONSTRAINED_RESIZE_POLICY);
//        tableView.setColumnResizePolicy(MY_RESIZE_POLICY);
//        // Sedrick: binding the prefWidth of first to sum of second and last
//        DoubleProperty width = new SimpleDoubleProperty();
//        width.bind(colMiddleName.widthProperty().add(colLastName.widthProperty()));
//        width.addListener((ov, t, t1) -> {
//            colFirstName.setPrefWidth(tableView.getWidth() - 5 - t1.doubleValue());
//        });
//
//        colFirstName.setPrefWidth(tableView.getWidth() - 5 - width.doubleValue());
//        // drawback: first must not be resizable
//        colFirstName.setResizable(false);
//
//        tableView.widthProperty().addListener((ov, t, t1) -> {
//            colFirstName.setPrefWidth(tableView.getWidth() - 5 - width.doubleValue());
//        });
    }
    

    private void configureColumn(TableColumn column, Callback factory, double minWidth, double prefWidth, double maxWidth) {
        column.setCellValueFactory(factory);
        if (minWidth >= 0) {
            column.setMinWidth(minWidth);
            column.setPrefWidth(prefWidth);
            column.setMaxWidth(maxWidth);
        }
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableColumnGrowSedrick.class.getName());
}

class Person {
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty middleName = new SimpleStringProperty();

    public Person(String firstName, String middleName, String lastName) {
        this.firstName.set(firstName);
        this.middleName.set(middleName);
        this.lastName.set(lastName);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getMiddleName() {
        return lastName.get();
    }

    public void setMiddleName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty middleNameProperty() {
        return lastName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }
}

/*
 * null
org.eclipse.recommenders.completion.rcp
Error
Wed Jul 31 11:22:32 CEST 2019
Session processor ‘class org.eclipse.recommenders.internal.calls.rcp.CallCompletionSessionProcessor’ failed with exception.

java.lang.NoClassDefFoundError: javax/annotation/PostConstruct
        at org.eclipse.recommenders.internal.rcp.RcpModule$Listener$1.afterInjection(RcpModule.java:278)
        at com.google.inject.internal.MembersInjectorImpl.notifyListeners(MembersInjectorImpl.java:97)
        at com.google.inject.internal.ConstructorInjector.construct(ConstructorInjector.java:95)
        at com.google.inject.internal.ConstructorBindingImpl$Factory.get(ConstructorBindingImpl.java:254)
        at com.google.inject.internal.ProviderToInternalFactoryAdapter$1.call(ProviderToInternalFactoryAdapter.java:46)
        at com.google.inject.internal.InjectorImpl.callInContext(InjectorImpl.java:1031)
        at com.google.inject.internal.ProviderToInternalFactoryAdapter.get(ProviderToInternalFactoryAdapter.java:40)
        at com.google.inject.Scopes$1$1.get(Scopes.java:65)
        at com.google.inject.internal.InternalFactoryToProviderAdapter.get(InternalFactoryToProviderAdapter.java:40)
        at com.google.inject.internal.FactoryProxy.get(FactoryProxy.java:54)
        at com.google.inject.internal.SingleParameterInjector.inject(SingleParameterInjector.java:38)
        at com.google.inject.internal.SingleParameterInjector.getAll(SingleParameterInjector.java:62)
        at com.google.inject.internal.ConstructorInjector.construct(ConstructorInjector.java:84)
        at com.google.inject.internal.ConstructorBindingImpl$Factory.get(ConstructorBindingImpl.java:254)
        at com.google.inject.internal.FactoryProxy.get(FactoryProxy.java:54)
        at com.google.inject.internal.ProviderToInternalFactoryAdapter$1.call(ProviderToInternalFactoryAdapter.java:46)
        at com.google.inject.internal.InjectorImpl.callInContext(InjectorImpl.java:1031)
        at com.google.inject.internal.ProviderToInternalFactoryAdapter.get(ProviderToInternalFactoryAdapter.java:40)
        at com.google.inject.Scopes$1$1.get(Scopes.java:65)
        at com.google.inject.internal.InternalFactoryToProviderAdapter.get(InternalFactoryToProviderAdapter.java:40)
        at com.google.inject.internal.InjectorImpl$4$1.call(InjectorImpl.java:978)
        at com.google.inject.internal.InjectorImpl.callInContext(InjectorImpl.java:1024)
        at com.google.inject.internal.InjectorImpl$4.get(InjectorImpl.java:974)
        at org.eclipse.recommenders.internal.calls.rcp.CallCompletionSessionProcessor.findReceiverTypeAndModel(CallCompletionSessionProcessor.java:136)
        at org.eclipse.recommenders.internal.calls.rcp.CallCompletionSessionProcessor.startSession(CallCompletionSessionProcessor.java:111)
        at org.eclipse.recommenders.completion.rcp.processable.IntelligentCompletionProposalComputer.fireStartSession(IntelligentCompletionProposalComputer.java:305)
        at org.eclipse.recommenders.completion.rcp.processable.IntelligentCompletionProposalComputer.computeCompletionProposals(IntelligentCompletionProposalComputer.java:171)
        at org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerDescriptor.computeCompletionProposals(CompletionProposalComputerDescriptor.java:336)
        at org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory.computeCompletionProposals(CompletionProposalCategory.java:340)
        at org.eclipse.jdt.internal.ui.text.java.ContentAssistProcessor.collectProposals(ContentAssistProcessor.java:334)
        at org.eclipse.jdt.internal.ui.text.java.ContentAssistProcessor.computeCompletionProposals(ContentAssistProcessor.java:291)
        at org.eclipse.jface.text.contentassist.ContentAssistant$2.lambda$0(ContentAssistant.java:2014)
        at java.base/java.util.Collections$SingletonSet.forEach(Collections.java:4795)
        at org.eclipse.jface.text.contentassist.ContentAssistant$2.run(ContentAssistant.java:2013)
        at org.eclipse.core.runtime.SafeRunner.run(SafeRunner.java:45)
        at org.eclipse.jface.text.contentassist.ContentAssistant.computeCompletionProposals(ContentAssistant.java:2010)
        at org.eclipse.jface.text.contentassist.CompletionProposalPopup.computeProposals(CompletionProposalPopup.java:575)
        at org.eclipse.jface.text.contentassist.CompletionProposalPopup.lambda$0(CompletionProposalPopup.java:505)
        at org.eclipse.swt.custom.BusyIndicator.showWhile(BusyIndicator.java:72)
        at org.eclipse.jface.text.contentassist.CompletionProposalPopup.showProposals(CompletionProposalPopup.java:500)
        at org.eclipse.jface.text.contentassist.ContentAssistant.showPossibleCompletions(ContentAssistant.java:1824)
        at org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor$AdaptedSourceViewer.doOperation(CompilationUnitEditor.java:189)
        at org.eclipse.ui.texteditor.ContentAssistAction.lambda$0(ContentAssistAction.java:85)
        at org.eclipse.swt.custom.BusyIndicator.showWhile(BusyIndicator.java:72)
        at org.eclipse.ui.texteditor.ContentAssistAction.run(ContentAssistAction.java:84)
        at org.eclipse.jface.action.Action.runWithEvent(Action.java:474)
        at org.eclipse.jface.commands.ActionHandler.execute(ActionHandler.java:121)
        at org.eclipse.ui.internal.handlers.E4HandlerProxy.execute(E4HandlerProxy.java:95)
        at jdk.internal.reflect.GeneratedMethodAccessor56.invoke(Unknown Source)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at org.eclipse.e4.core.internal.di.MethodRequestor.execute(MethodRequestor.java:58)
        at org.eclipse.e4.core.internal.di.InjectorImpl.invokeUsingClass(InjectorImpl.java:320)
        at org.eclipse.e4.core.internal.di.InjectorImpl.invoke(InjectorImpl.java:254)
        at org.eclipse.e4.core.contexts.ContextInjectionFactory.invoke(ContextInjectionFactory.java:173)
        at org.eclipse.e4.core.commands.internal.HandlerServiceHandler.execute(HandlerServiceHandler.java:156)
        at org.eclipse.core.commands.Command.executeWithChecks(Command.java:498)
        at org.eclipse.core.commands.ParameterizedCommand.executeWithChecks(ParameterizedCommand.java:488)
        at org.eclipse.e4.core.commands.internal.HandlerServiceImpl.executeHandler(HandlerServiceImpl.java:213)
        at org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher.executeCommand(KeyBindingDispatcher.java:308)
        at org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher.press(KeyBindingDispatcher.java:584)
        at org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher.processKeyEvent(KeyBindingDispatcher.java:653)
        at org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher.filterKeySequenceBindings(KeyBindingDispatcher.java:443)
        at org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher.access$2(KeyBindingDispatcher.java:386)
        at org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher$KeyDownFilter.handleEvent(KeyBindingDispatcher.java:96)
        at org.eclipse.swt.widgets.EventTable.sendEvent(EventTable.java:89)
        at org.eclipse.swt.widgets.Display.filterEvent(Display.java:1194)
        at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1055)
        at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1080)
        at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1065)
        at org.eclipse.swt.widgets.Widget.sendKeyEvent(Widget.java:1107)
        at org.eclipse.swt.widgets.Widget.sendKeyEvent(Widget.java:1103)
        at org.eclipse.swt.widgets.Widget.wmChar(Widget.java:1490)
        at org.eclipse.swt.widgets.Control.WM_CHAR(Control.java:4877)
        at org.eclipse.swt.widgets.Canvas.WM_CHAR(Canvas.java:353)
        at org.eclipse.swt.widgets.Control.windowProc(Control.java:4759)
        at org.eclipse.swt.widgets.Canvas.windowProc(Canvas.java:348)
        at org.eclipse.swt.widgets.Display.windowProc(Display.java:4770)
        at org.eclipse.swt.internal.win32.OS.DispatchMessage(Native Method)
        at org.eclipse.swt.widgets.Display.readAndDispatch(Display.java:3545)
        at org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine$5.run(PartRenderingEngine.java:1173)
        at org.eclipse.core.databinding.observable.Realm.runWithDefault(Realm.java:338)
        at org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine.run(PartRenderingEngine.java:1062)
        at org.eclipse.e4.ui.internal.workbench.E4Workbench.createAndRunUI(E4Workbench.java:155)
        at org.eclipse.ui.internal.Workbench.lambda$3(Workbench.java:635)
        at org.eclipse.core.databinding.observable.Realm.runWithDefault(Realm.java:338)
        at org.eclipse.ui.internal.Workbench.createAndRunWorkbench(Workbench.java:559)
        at org.eclipse.ui.PlatformUI.createAndRunWorkbench(PlatformUI.java:150)
        at org.eclipse.ui.internal.ide.application.IDEApplication.start(IDEApplication.java:155)
        at org.eclipse.equinox.internal.app.EclipseAppHandle.run(EclipseAppHandle.java:203)
        at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.runApplication(EclipseAppLauncher.java:137)
        at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.start(EclipseAppLauncher.java:107)
        at org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:400)
        at org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:255)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at org.eclipse.equinox.launcher.Main.invokeFramework(Main.java:660)
        at org.eclipse.equinox.launcher.Main.basicRun(Main.java:597)
        at org.eclipse.equinox.launcher.Main.run(Main.java:1468)
        at org.eclipse.equinox.launcher.Main.main(Main.java:1441)


 */ 
