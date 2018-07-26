package io.opensphere.mantle.iconproject.panels;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

/**
 * A panel in which a user may select a subcategory, create a new subcategory,
 * or select no subcategories.
 */
public class SubCollectPane extends VBox
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The combo box from which an existing category is selected. This is
     * disabled if the user is entering a new category.
     */
    private ComboBox<String> myComboBox;

    /**
     * The radio button with which the user selects the mode of the category
     * panel to use existing categories. This button is in a group with
     * {@link #myNewCatRB}, {@link #myNoneRB} and optionally
     * {@link #mySubCatsFromDirNamesRB}, and the group is used to put the panel
     * in a mode in which the user may select an existing group, or create a new
     * group.
     */
    private RadioButton myExistingRB;

    /**
     * The radio button with which the user selects the mode of the name panel
     * to create a new category. This button is in a group with
     * {@link #myExistingRB}, {@link #myNoneRB} and optionally
     * {@link #mySubCatsFromDirNamesRB}, and the group is used to put the panel
     * in a mode in which the user may select an existing group, or create a new
     * group.
     */
    private RadioButton myNewCatRB;

    /**
     * The radio button with which the user selects the mode of the name panel
     * to not use a category. This button is in a group with
     * {@link #myExistingRB}, {@link #myNewCatRB} and optionally
     * {@link #mySubCatsFromDirNamesRB}, and the group is used to put the panel
     * in a mode in which the user may select an existing group, or create a new
     * group.
     */
    private RadioButton myNoneRB;

    /**
     * The radio button with which the user selects the mode of the name panel
     * to create subcategories from directory names. This radio button may not
     * be instantiated, and is available if the constructor is called with a
     * value of <code>true</code> for its second argument. This button is in a
     * group with {@link #myExistingRB}, {@link #myNewCatRB}, and
     * {@link #myNoneRB}, and the group is used to put the panel in a mode in
     * which the user may select an existing group, or create a new group.
     */
    private RadioButton mySubCatsFromDirNamesRB;

    /**
     * The text field into which the user may enter a new category name. This
     * field is disabled if the user is selecting an existing category.
     */

    private ObservableList<String> myComboBoxItems;

    private ToggleGroup myToggleGroup = new ToggleGroup();

    private PanelModel myPanelModel;

    private IconRegistry myIconRegistry;

    private Set<String> myCategorySet;

    private boolean subCatsFromDirNames;

    private boolean myChoice;

    /**
     * Instantiates a new sub-category selection panel.
     *
     * @param categorySet the set of categories with which to populate the
     *            selection panel.
     * @param subCatsFromDirNames true if the user should be allowed to create
     *            subcategories from directory names, false otherwise (if
     *            <code>true</code>, this causes the instantiation of
     *            {@link #mySubCatsFromDirNamesRB}).
     */
    public SubCollectPane(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        myIconRegistry = myPanelModel.getMyIconRegistry();
        String name = "Default";
        myCategorySet = myIconRegistry.getSubCategoiresForCollection(name);
        List<String> names = New.list(myCategorySet);
        Collections.sort(names);
        myComboBoxItems = FXCollections.observableArrayList(names);
        myPanelModel.getMyCollectionName().addListener((observable, oldValue, newValue) -> updateComboBox());
        createPanel();
    }

    private void createPanel()
    {
        HBox hbox = new HBox();
        Label CollectionText = new Label("Do you want to add a sub-category to your icon?:");
        CollectionText.setFont(Font.font(CollectionText.getFont().getFamily(), FontPosture.ITALIC, 11));
        CollectionText.setContentDisplay(ContentDisplay.BOTTOM);

        myNoneRB = new RadioButton("No Sub-category");
        myNoneRB.setToggleGroup(myToggleGroup);

        myExistingRB = new RadioButton("Existing");
        myExistingRB.setToggleGroup(myToggleGroup);

        myComboBox = new ComboBox<>(myComboBoxItems);
        HBox existingPnl = new HBox();
        existingPnl.getChildren().addAll(myExistingRB, myComboBox);

        myNewCatRB = new RadioButton("New");
        myNewCatRB.setToggleGroup(myToggleGroup);

        hbox.setAlignment(Pos.BASELINE_LEFT);
        hbox.setSpacing(5.);

        if (myComboBoxItems.isEmpty())
        {
            hbox.getChildren().addAll(myNoneRB, myNewCatRB);
            System.out.println("it is empty");
        }
//        else if (myChoice)
//        {
//            mySubCatsFromDirNamesRB = new RadioButton("Create sub-categories from folder names.");
//            mySubCatsFromDirNamesRB.setTooltip(new Tooltip("Search for all sub-folders of existing folders"
//                    + " and add found icons with the folder name as the sub-category."));
//            mySubCatsFromDirNamesRB.setToggleGroup(myToggleGroup);
//            hbox.getChildren().add(mySubCatsFromDirNamesRB);
//        }
        else
        {
            System.out.println("it is not empty");
            hbox.getChildren().addAll(myNoneRB, myExistingRB, myNewCatRB, myComboBox);

        }
        
        getChildren().addAll(CollectionText, hbox);
        setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: blue;");

    }

    private void updateComboBox()
    {

        List<String> names = New.list(myIconRegistry.getSubCategoiresForCollection(myPanelModel.getMyCollectionName().get()));
        Collections.sort(names);
        myComboBoxItems = FXCollections.observableArrayList(names);
        getChildren().removeAll(getChildren());
        createPanel();
    }

    /**
     * Gets the category name.
     *
     * @return the category name
     */
    public String getSubCategory()
    {
        if (myNewCatRB.isSelected())
        {
            return myComboBox.getSelectionModel().getSelectedItem().toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Checks if is no category.
     *
     * @return true, if is no category
     */
    public boolean isNoCategory()
    {
        return myNoneRB.isSelected();
    }

    /**
     * Checks if is sub cats from dir names.
     *
     * @return true, if is sub cats from dir names
     */
    public boolean isSubCatsFromDirNames()
    {
        return mySubCatsFromDirNamesRB != null && mySubCatsFromDirNamesRB.isSelected();
    }
}
