/*
 * Created on 06.09.2016
 *
 */
//package de.swingempire.fx.control.pagination;

package de.saxsys.jfx.tabellen;

import java.time.LocalDate;
import java.util.function.Predicate;

//import com.jyloo.syntheticafx.FilterController;
//import com.jyloo.syntheticafx.PatternColumnFilter;
//import com.jyloo.syntheticafx.TableColumn;
//import com.jyloo.syntheticafx.TableView;
//
import de.saxsys.jfx.tabellen.model.Person;
import de.saxsys.jfx.tabellen.model.Town;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
//import javafx.scene.control.TableColumn;
//import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

/**
 * source: 
 * https://github.com/sialcasa/jfxtables
 * 
 * Pagination via FilteredList
 */
//TODO report bug reorder solvency
public class PersonTable extends TableView<Person> {

        private static final String DEFAULT_FILL = "";
        private final PersonService personsService;
        private final TownService townService;

        private final TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
        private final TableColumn<Person, LocalDate> ageCol = new TableColumn<>("Age");
        private final TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");

        private final TableColumn<Person, Town> townCol = new TableColumn<>("Town");
        private final TableColumn<Person, Town> townName = new TableColumn<>("Town Name");
        private final TableColumn<Person, Town> townZipCode = new TableColumn<>("Zip Code");

//        private final TableColumn<Person, Double> solvencyCol = new TableColumn<>("Solvency");
        private FilteredList<Person> virtualizedPersons;
//        private FilteredList<Person> filteredPersons;

        private Pagination pagination;
        
//        @Inject
        public PersonTable(PersonService personsService, TownService townsService) {
                this.personsService = personsService;
                this.townService = townsService;

                setEditable(true);

                initFirstNameCol();
                initLastNameCol();
                initAgeCol();
                initTownNameCol();
                initTownZipCol();
//                initSolvencyCol();

                addColumns();
                System.out.println("Table Init Done");

                // Size of Table to parent
                setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                // Loading of big data
                initPagination();

        }

        public Pagination getPagination() {
            return pagination;
        }
        
        /**
         * Need a FilterController that does not rebind on itemsChanged,
         * we manage the FilteredList binding manually.
         * <p>
         * PENDING JW: change logic in getFilteredList to return the
         * first on top of a not-transformationList? 
         */
//        @Override
//        protected FilterController createFilterController() {
////            FilterController controller = super.createFilterController();
//            FilterController controller = new FilterController(this) {
//
//                @Override
//                protected void itemsChanged(ObservableList old,
//                        ObservableList nv) {
//                    // by-pass super - manual binding
////                    super.itemsChanged(old, nv);
//                    setItemsFilterable(true);
//                }
//                
//            };
//            return controller;
//        }

        private void initPagination() {
                int itemCount = 10;
                ListProperty<Person> persons = personsService.getPersons();
                /*final Pagination*/ 
                pagination = new Pagination(persons.getValue().size() / itemCount);

                // this is the list for "real" filtering
//                filteredPersons = new FilteredList<>(persons);
                virtualizedPersons = persons.filtered(createPaginationPredicate(itemCount, persons));

                itemsProperty().bind(new SimpleListProperty<>(virtualizedPersons));
                pagination.setPageFactory(e -> {
                    virtualizedPersons.setPredicate(createPaginationPredicate(itemCount, persons));
                    return this;
                    });
                
                // manual binding to controller's predicate
//                filteredPersons.predicateProperty().bind((ObservableValue<? extends Predicate<? super Person>>) getFilterController().predicateProperty());
        }

        /**
         * @param itemCount
         * @param persons
         * @return
         */
        private Predicate<Person> createPaginationPredicate(int itemCount,
                ListProperty<Person> persons) {
            return t -> {
                    int indexOf = persons.indexOf(t);
                    int currentItemCount = pagination.getCurrentPageIndex() * itemCount;
                    if (indexOf > currentItemCount && indexOf < currentItemCount + itemCount)
                            return true;
                    else
                            return false;
            };
        }

        private void initFirstNameCol() {
                firstNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
                firstNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("firstname"));
//                firstNameCol.setColumnFilter(new PatternColumnFilter<>());
        }

        private void initLastNameCol() {
                lastNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
                lastNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("lastname"));
//                lastNameCol.setColumnFilter(new PatternColumnFilter<>());
        }

        private void initAgeCol() {
                ageCol.setCellValueFactory(new PropertyValueFactory<Person, LocalDate>("birth"));
                ageCol.setCellFactory(c -> new AgePickerCell<>());
                ageCol.setOnEditCommit(e -> {
                        System.out.println(e.getTableView());
                        e.getTableView().getItems().get(e.getTablePosition().getRow()).setBirth(e.getNewValue());
                });
        }

        private void initTownZipCol() {
                townZipCode.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Town>() {

                        @Override
                        public String toString(Town object) {
                                if (object == null) {
                                        return DEFAULT_FILL;
                                }
                                return String.valueOf(object.getZipCode());
                        }

                        @Override
                        public Town fromString(String string) {
                                try {
                                        return townService.getTownForZip(Integer.parseInt(string));
                                } catch (NumberFormatException e) {
                                        return null;
                                }

                        }
                }));
                townZipCode.setCellValueFactory(cellDataFeature -> cellDataFeature.getValue().townProperty());

        }

        private void initTownNameCol() {
                townName.setCellValueFactory(p -> p.getValue().townProperty());
                townName.setCellFactory(ComboBoxTableCell.<Person, Town> forTableColumn(new StringConverter<Town>() {

                        @Override
                        public String toString(Town object) {
                                if (object == null) {
                                        return DEFAULT_FILL;
                                }
                                return object.getName();
                        }

                        @Override
                        public Town fromString(String string) {
                                return townService.getTownForName(string);
                        }
                }, townService.getTowns().getValue()));

        }

//        private void initSolvencyCol() {
//                solvencyCol.setCellValueFactory(new PropertyValueFactory<Person, Double>("solvency"));
//                solvencyCol.setCellFactory(ProgressBarTableCell.forTableColumn());
//        }

        @SuppressWarnings("unchecked")
        private void addColumns() {

                // Combine Townname and Zip to Town
                townCol.getColumns().addAll(townName, townZipCode);

                // Add Major colums
                getColumns().addAll(firstNameCol, lastNameCol, ageCol, townCol); //, solvencyCol);

                itemsProperty().bind(new SimpleListProperty<>(personsService.getPersons()));
        }
}
