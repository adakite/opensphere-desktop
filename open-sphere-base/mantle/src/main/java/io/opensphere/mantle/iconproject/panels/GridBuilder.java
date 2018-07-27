package io.opensphere.mantle.iconproject.panels;

import java.awt.Window;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.view.IconCustomizerDialog;

/** Crates the Icon Display Grid. */
public class GridBuilder extends TilePane// implements Runnable
{
    /** The width used for icon buttons. */
    private final int myTileWidth;

    /** The icon registry used for the pane. */
    private IconRegistry myIconRegistry;

    /** The selected icon to be used for the builder. */
    private IconRecord mySelectedIcon;

    /** The icon record list. */
    List<IconRecord> myRecordList;

    private PanelModel myPanelModel;

    /**
     * The GridBuilder constructor. sets up the rows and columns for the icon
     * grid.
     *
     * @param tileWidth the width of each tile(button).
     * @param recList the icon record list
     * @param iconRegistry the icon registry
     */

    public GridBuilder(int tileWidth, List<IconRecord> recList, PanelModel thePanelModel)
    {
        myTileWidth = tileWidth;
        myPanelModel = thePanelModel;
        myIconRegistry = myPanelModel.getMyIconRegistry();
        myRecordList = recList;

        // System.out.println("making grid with: " + myRecordList);
        setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: purple;");

        for (IconRecord recordindex : myRecordList)
        {
            Button sample = buttonBuilder(recordindex);
            setMargin(sample, new Insets(5, 5, 5, 5));
            getChildren().add(sample);
        }
    }

    /* @Override public void run() {
     * System.out.println("&^^^^^^^^^^^^^^^^^^^^^is this running?");
     * setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" +
     * "-fx-border-width: 2;" + "-fx-border-insets: 5;" +
     * "-fx-border-radius: 5;" + "-fx-border-color: purple;");
     * 
     * for (IconRecord record : myRecordList) { Button sample =
     * buttonBuilder(record); setMargin(sample, new Insets(5, 5, 5, 5));
     * getChildren().add(sample); } } */

    /**
     * Creates the image buttons to be placed in the grid.
     *
     * @param record the IconRecord
     * @return generic the made button
     */
    private Button buttonBuilder(IconRecord record)
    {
        Button generic = new Button();
        generic.setMinSize(myTileWidth, myTileWidth);
        generic.setMaxSize(myTileWidth, myTileWidth);
        String text = record.getName();
        generic.setPadding(new Insets(5, 5, 5, 5));
        generic.setTooltip(new Tooltip(text));

        generic.setText(text);
        generic.setContentDisplay(ContentDisplay.TOP);
        generic.setAlignment(Pos.BOTTOM_CENTER);

        ImageView iconView = new ImageView(record.getImageURL().toString());
        if (iconView.getImage().getWidth() > myTileWidth)
        {
            iconView.setFitHeight(myTileWidth - 25);
            iconView.setFitWidth(myTileWidth - 25);
        }
        generic.setGraphic(iconView);

        generic.setOnAction(e ->
        {
            myPanelModel.setIconRecord(record);
        });

        return generic;
    }

    /**
     * Shows the icon customizer.
     *
     * @param tb the toolbox.
     * @param owner the current window pane.
     */
    public void showIconCustomizer(Window owner)
    {
        IconCustomizerDialog builderPane = new IconCustomizerDialog(owner, myPanelModel);
        builderPane.setVisible(true);
    }

    /**
     * Clears the gridPane.
     */
    public void refresh()
    {
        getChildren().clear();
        System.out.println("clearing yo");
        // new GridBuilder(myTileWidth, myIconRegistry);
    }

}