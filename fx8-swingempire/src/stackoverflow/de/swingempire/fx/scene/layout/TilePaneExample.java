/*
 * Created on 11.07.2018
 *
 */
package de.swingempire.fx.scene.layout;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static javafx.geometry.Orientation.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Requirement is to layout such that it is resized vertically only if
 * next tile fits.
 * 
 * Idea: let super do its layout, then relocate the tiles in the last row.
 * 
 * Doesn't work, endless loop - why? 
 * 
 * Next idea: c&p all layout code and let lastRowRemainder be 0 always.
 * 
 * https://stackoverflow.com/q/51279127/203657
 */
public class TilePaneExample extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final VBox root = new VBox();
//        BorderPane root = new BorderPane();
        final Scene sc = new Scene(root);
        primaryStage.setScene(sc);

        final TilePane tp = new TilePane() {

            @Override
            protected double computePrefWidth(double forHeight) {
                double orig = super.computePrefWidth(forHeight);
//                LOG.info("" + ((Region) getParent()).getWidth());
                return orig;
            }

//            @Override
//            protected void layoutChildren() {
////                LOG.info("parent/my width" + ((Region) getParent()).getWidth() + "/ " + computePrefWidth(-1));
//              double delta = getWidth() - computePrefWidth(-1);
//                super.layoutChildren();
//
//                List<Node> managed = getManagedChildren();
//                HPos hpos = getAlignmentInternal().getHpos();
//                VPos vpos = getAlignmentInternal().getVpos();
//                double width = getWidth();
//                double height = getHeight();
//                double top = snapSpaceY(getInsets().getTop());
//                double left = snapSpaceX(getInsets().getLeft());
//                double bottom = snapSpaceY(getInsets().getBottom());
//                double right = snapSpaceX(getInsets().getRight());
//                double vgap = snapSpaceY(getVgap());
//                double hgap = snapSpaceX(getHgap());
//                double insideWidth = width - left - right;
//                double insideHeight = height - top - bottom;
//
//                double tileWidth = getTileWidth() > insideWidth ? insideWidth : getTileWidth();
//                double tileHeight = getTileHeight() > insideHeight ? insideHeight : getTileHeight();
//                
//                int actRows = getActualRows();
//                int actColumns = getActualColumns();
//                
//                int columns = computeColumns(insideWidth, tileWidth);
//                int rows = computeOther(managed.size(), columns);
//                if (delta != 0 && actRows > 1) {
//                    int upperTileCount = columns * (rows - 1);
//                    double rowX = left + computeXOffset(insideWidth,
//                            computeContentWidth(actColumns, tileWidth),
//                            hpos);
//                    double columnY = top + computeYOffset(insideHeight,
//                            computeContentHeight(actRows, tileHeight),
//                            vpos);
//                    double baselineOffset = 0;
////                            getTileAlignmentInternal().getVpos() == VPos.BASELINE ?
////                            getAreaBaselineOffset(managed, marginAccessor, i -> tileWidth, tileHeight, false) : -1;
//
//
//
//                    for (int tile = upperTileCount; tile < managed.size(); tile++) {
//                        Node child = managed.get(tile);
//                        // can't - looping forever
////                        node.relocate(rowX, columnY + tileHeight);
//                        double xoffset = rowX;//r == (actualRows - 1)? lastRowX : rowX;
//                        double yoffset = columnY + tileHeight + vgap;// c == (actualColumns - 1)? lastColumnY : columnY;
//
//                        double tileX = xoffset; // + (c * (tileWidth + hgap));
//                        double tileY = yoffset; //+ (r * (tileHeight + vgap));
//
//                        Pos childAlignment = getAlignment(child);
//
//                        layoutInArea(child, tileX, tileY, tileWidth, tileHeight, baselineOffset,
//                                getMargin(child),
//                                childAlignment != null? childAlignment.getHpos() : getTileAlignmentInternal().getHpos(),
//                                childAlignment != null? childAlignment.getVpos() : getTileAlignmentInternal().getVpos());
//
//                        LOG.info("tile: " + tile);
////                        LOG.info(": " + actRows + " / " + rows + " / " + upperTileCount);
//                        
//                    }
//                    
//                }
////                LOG.info("" + ((Region) getParent()).getWidth());
//            }
//
            @Override protected void layoutChildren() {
                List<Node> managed = getManagedChildren();
                HPos hpos = getAlignmentInternal().getHpos();
                VPos vpos = getAlignmentInternal().getVpos();
                double width = getWidth();
                double height = getHeight();
                double top = snapSpaceY(getInsets().getTop());
                double left = snapSpaceX(getInsets().getLeft());
                double bottom = snapSpaceY(getInsets().getBottom());
                double right = snapSpaceX(getInsets().getRight());
                double vgap = snapSpaceY(getVgap());
                double hgap = snapSpaceX(getHgap());
                double insideWidth = width - left - right;
                double insideHeight = height - top - bottom;

                double tileWidth = getTileWidth() > insideWidth ? insideWidth : getTileWidth();
                double tileHeight = getTileHeight() > insideHeight ? insideHeight : getTileHeight();

                int lastRowRemainder = 0;
                int lastColumnRemainder = 0;
                int actualColumns = 0;
                int actualRows = 0;
                if (getOrientation() == HORIZONTAL) {
                    actualColumns = computeColumns(insideWidth, tileWidth);
                    actualRows = computeOther(managed.size(), actualColumns);
                    // remainder will be 0 if last row is filled
                    lastRowRemainder = 0;
//                            hpos != HPos.LEFT?
//                         actualColumns - (actualColumns*actualRows - managed.size()) : 0;
                } else {
                    // vertical
                    actualRows = computeRows(insideHeight, tileHeight);
                    actualColumns = computeOther(managed.size(), actualRows);
                    // remainder will be 0 if last column is filled
                    lastColumnRemainder = vpos != VPos.TOP?
                        actualRows - (actualColumns*actualRows - managed.size()) : 0;
                }
                double rowX = left + computeXOffset(insideWidth,
                                                    computeContentWidth(actualColumns, tileWidth),
                                                    hpos);
                double columnY = top + computeYOffset(insideHeight,
                                                    computeContentHeight(actualRows, tileHeight),
                                                    vpos);

                double lastRowX = lastRowRemainder > 0?
                                  left + computeXOffset(insideWidth,
                                                    computeContentWidth(lastRowRemainder, tileWidth),
                                                    hpos) :  rowX;
                double lastColumnY = lastColumnRemainder > 0?
                                  top + computeYOffset(insideHeight,
                                                    computeContentHeight(lastColumnRemainder, tileHeight),
                                                    vpos) : columnY;
                double baselineOffset = -1; 
//                getTileAlignmentInternal().getVpos() == VPos.BASELINE ?
//                        getAreaBaselineOffset(managed, marginAccessor, i -> tileWidth, tileHeight, false) : -1;

                int r = 0;
                int c = 0;
                for (int i = 0, size = managed.size(); i < size; i++) {
                    Node child = managed.get(i);
                    double xoffset = r == (actualRows - 1)? lastRowX : rowX;
                    double yoffset = c == (actualColumns - 1)? lastColumnY : columnY;

                    double tileX = xoffset + (c * (tileWidth + hgap));
                    double tileY = yoffset + (r * (tileHeight + vgap));

                    Pos childAlignment = getAlignment(child);

                    layoutInArea(child, tileX, tileY, tileWidth, tileHeight, baselineOffset,
                            getMargin(child),
                            childAlignment != null? childAlignment.getHpos() : getTileAlignmentInternal().getHpos(),
                            childAlignment != null? childAlignment.getVpos() : getTileAlignmentInternal().getVpos());

                    if (getOrientation() == HORIZONTAL) {
                        if (++c == actualColumns) {
                            c = 0;
                            r++;
                        }
                    } else {
                        // vertical
                        if (++r == actualRows) {
                            r = 0;
                            c++;
                        }
                    }
                }
            }

//            private int actualRows = 0;
//            private int actualColumns = 0;

            private double computeContentWidth(int columns, double tilewidth) {
                if (columns == 0) return 0;
                return columns * tilewidth + (columns - 1) * snapSpaceX(getHgap());
            }

            private double computeContentHeight(int rows, double tileheight) {
                if (rows == 0) return 0;
                return rows * tileheight + (rows - 1) * snapSpaceY(getVgap());
            }


            private int computeColumns(double width, double tilewidth) {
                double snappedHgap = snapSpaceX(getHgap());
                return Math.max(1,(int)((width + snappedHgap) / (tilewidth + snappedHgap)));
            }

            private int computeRows(double height, double tileheight) {
                double snappedVgap = snapSpaceY(getVgap());
                return Math.max(1, (int)((height + snappedVgap) / (tileheight + snappedVgap)));
            }

            private int computeOther(int numNodes, int numCells) {
                double other = (double)numNodes/(double)Math.max(1, numCells);
                return (int)Math.ceil(other);
            }
            private Pos getAlignmentInternal() {
                Pos localPos = getAlignment();
                return localPos == null ? Pos.TOP_LEFT : localPos;
            }
            private Pos getTileAlignmentInternal() {
                Pos localPos = getTileAlignment();
                return localPos == null ? Pos.CENTER : localPos;
            }

           private double computeXOffset(double width, double contentWidth, HPos hpos) {
                switch(hpos) {
                    case LEFT:
                        return 0;
                    case CENTER:
                        return (width - contentWidth) / 2;
                    case RIGHT:
                        return width - contentWidth;
                    default:
                        throw new AssertionError("Unhandled hPos");
                }
            }

            private double computeYOffset(double height, double contentHeight, VPos vpos) {
                switch(vpos) {
                    case BASELINE:
                    case TOP:
                        return 0;
                    case CENTER:
                        return (height - contentHeight) / 2;
                    case BOTTOM:
                        return height - contentHeight;
                    default:
                        throw new AssertionError("Unhandled vPos");
                }
            }

            private int getActualRows() {
                return (int) FXUtils.invokeGetFieldValue(TilePane.class, this, "actualRows");
            }
            
            private int getActualColumns() {
                return (int) FXUtils.invokeGetFieldValue(TilePane.class, this, "actualColumns");
            }
            
            
        };
        
//        root.setCenter(tp);
        root.getChildren().add(tp);
        root.setAlignment(Pos.CENTER);
        root.setFillWidth(true);

        tp.setPrefColumns(3);
        tp.setAlignment(Pos.CENTER);
        tp.setStyle("-fx-background-color: green;");

        Stream.iterate(0, i -> i + 1).limit(5).forEach(i -> {
            Region r = new Region();
            r.setPrefSize(200, 200);
            r.setStyle("-fx-background-color: red; -fx-border-color: blue; -fx-border-width: 1;");

            tp.getChildren().add(r);
        });

        Button b = new Button();
        primaryStage.show();
    }

    /**
     * Solution by fabian: adjust prefColumns to width of parent.
     */
//    @Override
//    public void start(Stage primaryStage) {
//        final VBox root = new VBox();
//        final Scene sc = new Scene(root);
//        primaryStage.setScene(sc);
//
//        final TilePane tp = new TilePane();
//        tp.setPrefTileWidth(200);
//        tp.setPrefTileHeight(200);
//        root.getChildren().add(tp);
//
//        tp.setPrefColumns(3);
//        tp.setAlignment(Pos.TOP_LEFT);
//        tp.setStyle("-fx-background-color: green;");
//        root.setAlignment(Pos.CENTER);
//        root.setFillWidth(false);
//
//        // set prefColumns from a listener instead of a binding
//        // to prevent the initial value from being set to 0
//        root.widthProperty().addListener((o, oldValue, newValue) -> {
//            // allow as many columns as fit the parent but keep it in
//            // range [1, childCount]
//            tp.setPrefColumns(Math.min(tp.getChildren().size(),
//                    Math.max(1, (int) (newValue.doubleValue() / tp.getPrefTileWidth()))));
//        });
//
//        Stream.iterate(0, i -> i + 1).limit(5).forEach(i -> {
//            Region r = new Region();
//            r.setStyle("-fx-background-color: red; -fx-border-color: blue; -fx-border-width: 1;");
//
//            tp.getChildren().add(r);
//        });
//
//        primaryStage.show();
//    } 


    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TilePaneExample.class.getName());

}
