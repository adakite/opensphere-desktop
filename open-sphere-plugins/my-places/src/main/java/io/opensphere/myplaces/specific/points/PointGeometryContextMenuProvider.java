package io.opensphere.myplaces.specific.points;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JSeparator;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.OverridingContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.control.action.context.ScreenPositionContextKey;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.editor.controller.AnnotationEditController;
import io.opensphere.myplaces.models.DataCouple;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.MyPointsMenuItemProvider;
import io.opensphere.myplaces.specific.points.utils.PointUtils;
import io.opensphere.myplaces.util.GroupUtils;

/**
 * The Class PointGeometryContextMenuProvider.
 */
public class PointGeometryContextMenuProvider extends MyPointsMenuItemProvider
        implements OverridingContextMenuProvider<GeometryContextKey>
{
    /** The default context menu provider. */
    private final ContextMenuProvider<ScreenPositionContextKey> myDefaultContextMenuProvider = new ContextMenuProvider<ScreenPositionContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, ScreenPositionContextKey key)
        {
            final GeographicPosition pos = myController.convertPointToGeographicPosition(key.getPosition().asPoint());
            if (pos != null)
            {
                JMenuItem mi = new JMenuItem("Create map point here...");
                mi.addActionListener(e -> myTypeController.createAnnotationPointFromPosition(pos));
                return Collections.singletonList(mi);
            }
            else
            {
                return null;
            }
        }

        @Override
        public int getPriority()
        {
            return 11000;
        }
    };

    /** The Last picked id. */
    private long myLastPickedId;

    /** The Places model. */
    private final MyPlacesModel myPlacesModel;

    /** The Controller. */
    private final AnnotationEditController myController;

    /**
     * The type controller.
     */
    private final PointTypeController myTypeController;

    /**
     * Instantiates a new test context menu provider.
     *
     * @param typeController The controller specific to points.
     * @param controller the controller
     * @param model the model
     */
    public PointGeometryContextMenuProvider(PointTypeController typeController, AnnotationEditController controller,
            MyPlacesModel model)
    {
        myPlacesModel = model;
        myLastPickedId = -1;
        myController = controller;
        myTypeController = typeController;

        ContextActionManager actionManager = controller.getToolbox().getUIRegistry().getContextActionManager();
        actionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class,
                this);
        actionManager.registerContextMenuItemProvider(ContextIdentifiers.SCREEN_POSITION_CONTEXT, ScreenPositionContextKey.class,
                myDefaultContextMenuProvider);
    }

    /**
     * Close the context menu provider.
     */
    public void close()
    {
        ContextActionManager actionManager = myController.getToolbox().getUIRegistry().getContextActionManager();
        actionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class,
                this);
        actionManager.deregisterContextMenuItemProvider(ContextIdentifiers.SCREEN_POSITION_CONTEXT,
                ScreenPositionContextKey.class, myDefaultContextMenuProvider);
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, GeometryContextKey key)
    {
        List<Component> menuItems = New.list();

        Geometry geom = key.getGeometry();
        myLastPickedId = geom.getDataModelId();
        Placemark result = myPlacesModel.findPlacemark(myLastPickedId);

        if (result != null)
        {
            for (ItemType type : ItemType.values())
            {
                if (type.isRequired())
                {
                    menuItems.add(getMenuItems().get(type));
                }
                else
                {
                    addMenuItems(menuItems, result, type);
                }

                if (type.isGroup())
                {
                    menuItems.add(new JSeparator(Separator.HORIZONTAL));
                }
            }
        }

        return menuItems;
    }

    @Override
    public int getPriority()
    {
        return 11000;
    }

    @Override
    public Collection<String> getOverrideItems()
    {
        // Override the Hide option provided by Overlay plugin
        return Collections.singleton("Hide");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Placemark placemark = myPlacesModel.findPlacemark(myLastPickedId);
        if (placemark != null)
        {
            Object src = e.getSource();
            if (getMenuItems().get(ItemType.CENTER_ON) == src)
            {
                myController.centerOnPoint(PointUtils.fromKml(placemark, this));
            }
            else if (getMenuItems().get(ItemType.EDIT) == src)
            {
                DataCouple couple = myController.getDataType(placemark.getId());
                couple.getDataType().launchEditor(couple.getDataGroup(), Collections.singletonList(couple.getDataType()));
            }
            else if (getMenuItems().get(ItemType.DELETE) == src)
            {
                int response = JOptionPane.showConfirmDialog(
                        myController.getToolbox().getUIRegistry().getMainFrameProvider().get(),
                        "Are you sure you want to delete " + placemark.getName() + "?", "Delete Confirmation",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.OK_OPTION)
                {
                    myController.deletePoint(placemark.getId());
                }
            }
            else if (getMenuItems().get(ItemType.HIDE_POINT) == src)
            {
                DataCouple couple = GroupUtils.getDataTypeAndParent(placemark.getId(), myPlacesModel.getDataGroups());
                doMenuItemAction(couple.getDataType(), placemark, Constants.IS_FEATURE_ON_ID, false);
            }
            else if (getMenuItems().get(ItemType.SHOW_POINT) == src)
            {
                DataCouple couple = GroupUtils.getDataTypeAndParent(placemark.getId(), myPlacesModel.getDataGroups());
                doMenuItemAction(couple.getDataType(), placemark, Constants.IS_FEATURE_ON_ID, true);
            }
            else if (getMenuItems().get(ItemType.SHOW_BUBBLE) == src)
            {
                DataCouple couple = GroupUtils.getDataTypeAndParent(placemark.getId(), myPlacesModel.getDataGroups());
                doMenuItemAction(couple.getDataType(), placemark, Constants.IS_ANNOHIDE_ID, false);
            }
            else if (getMenuItems().get(ItemType.HIDE_BUBBLE) == src)
            {
                DataCouple couple = GroupUtils.getDataTypeAndParent(placemark.getId(), myPlacesModel.getDataGroups());
                doMenuItemAction(couple.getDataType(), placemark, Constants.IS_ANNOHIDE_ID, true);
            }
        }
    }
}
