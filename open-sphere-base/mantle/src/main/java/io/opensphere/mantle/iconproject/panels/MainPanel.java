package io.opensphere.mantle.iconproject.panels;

import java.awt.EventQueue;
import java.awt.Window;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.impl.ButtonBuilder;
import io.opensphere.mantle.iconproject.view.AddIconDialog;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class Main Panel.
 *
 */
public class MainPanel extends SplitPane
{
    /** The Icon registry. */
    private final IconRegistry myIconRegistry;

    /** The Icon Display Grid. */
    private final GridBuilder myIconGrid;

    /** The Customize Icon button. */
    private final ButtonBuilder myCustIconButton = new ButtonBuilder("Customize Icon", false);

    /** The button to add the icon. */
    private final ButtonBuilder myAddIconButton = new ButtonBuilder("Add Icon from File", false);

    /** The button to generate a new icon. */
    private final ButtonBuilder myGenIconButton = new ButtonBuilder("Generate New Icon", false);

    /** The tree view. */
    private final TreeView<String> myTreeView;

    /** The left Panel. */
    private final AnchorPane myLeftView;

    private final TreeBuilder treeBuilder;

    Map<String, List<IconRecord>> recordMap = new HashMap<>();

    /**
     * The MainPanel constructor.
     *
     * @param tb the toolbox
     * @param owner the window owner
     */
    public MainPanel(Toolbox tb, Window owner)
    {
        myIconRegistry = MantleToolboxUtils.getMantleToolbox(tb).getIconRegistry();
        myLeftView = new AnchorPane();

        treeBuilder = new TreeBuilder(myIconRegistry, null);
        myTreeView = new TreeView<>(treeBuilder);

        recordMap = new HashMap<>(treeBuilder.getRecordMap());

        List<IconRecord> recordList = recordMap.get("GoogleEarth");

        myIconGrid = new GridBuilder(90, recordList, myIconRegistry);

        setDividerPositions(0.25, 0.98);
        setLayoutY(48.0);

        AnchorPane.setBottomAnchor(myTreeView, 78.0);
        AnchorPane.setLeftAnchor(myTreeView, 0.0);
        AnchorPane.setRightAnchor(myTreeView, 0.0);
        AnchorPane.setTopAnchor(myTreeView, 0.0);
        myTreeView.setLayoutY(8.0);

        myAddIconButton.lockButton(myAddIconButton);
        AnchorPane.setBottomAnchor(myAddIconButton, 52.0);
        myAddIconButton.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                AddIconDialog iconImporter = new AddIconDialog(owner, myIconRegistry);
                iconImporter.setVisible(true);
            });
        });

        AnchorPane.setBottomAnchor(myCustIconButton, 26.0);
        myCustIconButton.lockButton(myCustIconButton);
        myCustIconButton.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                myIconGrid.showIconCustomizer(tb, owner);
            });
        });

        AnchorPane.setBottomAnchor(myGenIconButton, 0.0);
        myGenIconButton.lockButton(myGenIconButton);
        myGenIconButton.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                //TODO:
            });
        });

        ScrollPane myScrollPane = new ScrollPane(myIconGrid);
        myScrollPane.setPannable(true);
        AnchorPane.setLeftAnchor(myScrollPane, 0.);
        AnchorPane.setRightAnchor(myScrollPane, 0.);
        AnchorPane.setTopAnchor(myScrollPane, 0.);
        AnchorPane.setBottomAnchor(myScrollPane, 0.);
        myScrollPane.setFitToHeight(true);
        myScrollPane.setFitToWidth(true);

        myTreeView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> treeHandle(newValue));

        myLeftView.getChildren().addAll(myTreeView, myAddIconButton, myCustIconButton, myGenIconButton);
        getItems().addAll(myLeftView, myScrollPane);
    }

    /**
     * The tree event handler.
     *
     * @param newValue the new clicked-on Value
     */
    private void treeHandle(TreeItem<String> newValue)
    {
        System.out.println("Choice is:  " + newValue.getValue());
        //System.out.println(" type is: " + treeBuilder.getIconTreeObject().getNameType());
        //myIconGrid.setTheChosen(newValue.getValue());//theChoice);
        //myIconGrid.setMyIconRegistry(myIconRegistry);
        //myIconGrid.refresh();

        ////////
        //System.out.println("The type is: " + treeBuilder.getIconTreeObject().getType());
        //System.out.println("the records are: " + treeBuilder.getIconTreeObject().getRecords(true));
        /*
        if(treeBuilder.getIconTreeObject().getNameType().toString().equals("COLLECTION"))
        {
            String parent = treeBuilder.getIconTreeObject().getParent();
            System.out.println("parent is:  " + parent);
            System.out.println("the actual records are: " + recordMap.get(newValue.getValue()));
            myIconGrid.setMyRecordList(recordMap.get(newValue.getValue()));
        }
        else
        {
            //TreeItem<String> parent =
            String parent = treeBuilder.getIconTreeObject().getParent();
            System.out.println("parent is:  " + parent);

            //myIconGrid.setMyRecordList(record);
        }*/
    }

    /**
     * Changes the Icon Display Grid from Grid to List.
     *
     * @param choice the selected toggle.
     */
    static void changeTop(boolean choice)
    {
        //        StackPane stackPane = new StackPane();
        //        ObservableList<Node> childs = stackPane.getChildren();
        //
        //        Node grid = childs.get(1);
        //        Node list = childs.get(0);
        //        if (choice)
        //        {
        //            grid.setVisible(false);
        //            list.setVisible(true);
        //        }
        //        else
        //        {
        //            list.setVisible(false);
        //            grid.setVisible(true);
        //        }

    }
}