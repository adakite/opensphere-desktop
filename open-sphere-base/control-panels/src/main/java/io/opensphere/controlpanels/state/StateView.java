package io.opensphere.controlpanels.state;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.export.Exporter;
import io.opensphere.core.export.Exporters;
import io.opensphere.core.modulestate.SaveStateDialog;
import io.opensphere.core.modulestate.StateData;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.SplitButton;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;

/**
 * Implementation of the view for the state plugin.
 */
class StateView
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StateView.class);

    /** The state controller. */
    private final StateController myController;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** Optional menu bar registry. */
    private final MenuBarRegistry myMenuBarRegistry;

    /** The preferences registry used to remember the last save location. */
    private final PreferencesRegistry myPrefsRegistry;

    /** The state control split button. */
    private final SplitButton /* IconButton */ myStateControlButton;

    /**
     * Constructor.
     *
     * @param controller The state controller.
     * @param toolbox The toolbox.
     */
    public StateView(StateController controller, Toolbox toolbox)
    {
        myController = Utilities.checkNull(controller, "controller");
        myToolbox = toolbox;
        myPrefsRegistry = Utilities.checkNull(toolbox.getPreferencesRegistry(), "prefsRegistry");
        myMenuBarRegistry = Utilities.checkNull(toolbox.getUIRegistry().getMenuBarRegistry(), "menuBarRegistry");

        myStateControlButton = new SplitButton("States", new GenericFontIcon(AwesomeIconSolid.BOOKMARK, Color.YELLOW), false)
        {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            protected List<Component> getDynamicMenuItems()
            {
                Collection<? extends String> states = myController.getAvailableStates();
                List<Component> menuItems = New.list(states.size());
                for (String state : states)
                {
                    String stateDescription = myController.getStateDescription(state);
                    JMenuItem menuItem = new JCheckBoxMenuItem(new ToggleStateAction(state));
                    menuItem.setToolTipText(StringUtilities.concat("Toggle ", state, " (description: ",
                            stateDescription.isEmpty() ? "empty" : stateDescription, ")"));
                    menuItem.setHorizontalTextPosition(SwingConstants.LEFT);
//                            createAlignedMenuItem(
//                                    new ToggleStateAction(state), null, StringUtilities.concat("Toggle ", state,
//                                            " (description: ", stateDescription.isEmpty() ? "empty" : stateDescription, ")"),
//                                    true);
                    menuItems.add(menuItem);
                }
                return menuItems;
            }
        };
//        myStateControlButton = new IconButton("States", new GenericFontIcon(AwesomeIconSolid.BOOKMARK, Color.YELLOW));
        myStateControlButton.setToolTipText("States controls");

//        JPopupMenu popupMenu = new JPopupMenu();
//        myStateControlButton.addActionListener(new ActionListener()
//        {
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                popupMenu.setVisible(true);
//            }
//        });
        /* (new MouseAdapter() { public void mousePressed(MouseEvent e) { popupMenu.show(e.getComponent(), e.getX(), e.getY()); }}); */

        JMenuItem importMenuItem = new JMenuItem();
        importMenuItem.setIcon(new GenericFontIcon(AwesomeIconSolid.CLOUD_DOWNLOAD_ALT, Color.WHITE));
        importMenuItem.setToolTipText("Import a state from url or file");
        myStateControlButton.addMenuItem(importMenuItem);

        JMenuItem saveMenuItem = new JMenuItem(new SaveStateAction());
//                createAlignedMenuItem(new SaveStateAction(),
//                new GenericFontIcon(AwesomeIconSolid.SAVE, Color.WHITE), "Save the current application state", false);
        saveMenuItem.setIcon(new GenericFontIcon(AwesomeIconSolid.SAVE, Color.WHITE));
        saveMenuItem.setToolTipText("Save the current application state");
        myStateControlButton.addMenuItem(saveMenuItem);
//        popupMenu.add(saveMenuItem);

        JMenuItem disableMenuItem = new JMenuItem(new DisableStatesAction());
//                createAlignedMenuItem(new DisableStatesAction(),
//                new GenericFontIcon(AwesomeIconSolid.TIMES, Color.WHITE), "Deactivate all states", false);
        disableMenuItem.setIcon(new GenericFontIcon(AwesomeIconSolid.TIMES, Color.WHITE));
        disableMenuItem.setToolTipText("Deactivate all states");
        myStateControlButton.addMenuItem(disableMenuItem);
//        popupMenu.add(disableMenuItem);

        JMenuItem deleteMenuItem = new JMenuItem(new DeleteStatesAction());
//                createAlignedMenuItem(new DeleteStatesAction(),
//                new GenericFontIcon(AwesomeIconSolid.TRASH_ALT, Color.WHITE), "Delete states from the application", false);
        deleteMenuItem.setIcon(new GenericFontIcon(AwesomeIconSolid.TRASH_ALT, Color.WHITE));
        deleteMenuItem.setToolTipText("Remove states from the application");
        myStateControlButton.addMenuItem(deleteMenuItem);
//        popupMenu.add(deleteMenuItem);

//        myStateControlButton.add(new JSeparator());
    }

//    /**
//     * Creates a menu item with either an icon or a checkbox. This allows the menu items with checkboxes to line up neatly with the menu items with icons.
//     *
//     * @param action the action of the menu item
//     * @param icon the icon for the menu item, or null
//     * @param toolTipText the tooltip for the menu item
//     * @param useCheckBox if a checkbox menu item should be returned
//     * @return the aligned menu item with either a checkbox or icon
//     */
//    private JMenuItem createAlignedMenuItem(Action action, Icon icon, String toolTipText, boolean useCheckBox)
//    {
//        JMenuItem alignedMenuItem = useCheckBox ? new JCheckBoxMenuItem() : new JMenuItem();
////                new JCheckBoxMenuItem(action);
//        alignedMenuItem.setAction(action);
//        alignedMenuItem.setIcon(icon);
//        alignedMenuItem.setToolTipText(toolTipText);
//        alignedMenuItem.setMargin(new Insets(5, 25, 5, 5));
//        alignedMenuItem.setIconTextGap(15);
//        alignedMenuItem.setHorizontalTextPosition(SwingConstants.RIGHT);
//
//        return alignedMenuItem;
//    }

    /**
     * Get the state control button.
     *
     * @return The button.
     */
    public SplitButton /* IconButton */ getStateControlButton()
    {
        return myStateControlButton;
    }

    /**
     * Perform a save state.
     */
    protected void saveState()
    {
        Window parent = SwingUtilities.getWindowAncestor(myStateControlButton);

        Collection<? extends String> disallowedStateNames = myController.getAvailableStates();

        JCheckBox saveToFile = new JCheckBox("File", false);
        saveToFile.setToolTipText("Export the state to a file");

        JCheckBox saveToApp = new JCheckBox(
                StringUtilities.expandProperties(System.getProperty("opensphere.title", "Application"), System.getProperties()),
                true);
        saveToApp.setToolTipText("Make the state available in the application");

        List<JCheckBox> saveTos = New.list(saveToFile, saveToApp);
        saveTos.add(saveToFile);
        saveTos.add(saveToApp);

        Map<JCheckBox, Exporter> exporterMap = New.map();
        StateData dummyData = new StateData("123", "desc", Collections.emptyList(), Collections.emptyList());
        List<Exporter> exporters = Exporters.getExporters(Collections.singletonList(dummyData), myToolbox, null);
        for (Exporter exporter : exporters)
        {
            JCheckBox checkBox = new JCheckBox(exporter.getMimeTypeString());
            saveTos.add(checkBox);
            exporterMap.put(checkBox, exporter);
        }

        Collection<? extends String> modulesThatCanSaveState = myController.getModulesThatCanSaveState();
        Map<String, Collection<? extends String>> stateDependencies =
                myController.getStateDependenciesForModules(myController.getModulesThatSaveStateByDefault());
        SaveStateDialog dialog =
                new SaveStateDialog(parent, modulesThatCanSaveState, stateDependencies, disallowedStateNames, saveTos);

        saveToApp.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dialog.setDisallowedNames(saveToApp.isSelected() ? disallowedStateNames : Collections.<String>emptyList());
            }
        });

        dialog.build();
        dialog.setSelectedModules(myController.getModulesThatSaveStateByDefault());

        boolean retry = true;
        while (retry)
        {
            dialog.showDialog();
            if (dialog.getSelection() == JOptionPane.OK_OPTION)
            {
                StateData data = new StateData(dialog.getStateId(), dialog.getDescription(), dialog.getTags(),
                        dialog.getSelectedModules());
                retry = doSaveState(data, dialog.getParent(), saveToFile.isSelected(), saveToApp.isSelected(), saveTos,
                        exporterMap);
            }
            else
            {
                retry = false;
            }
        }
    }

    /**
     * Save the state after the options have been selected.
     *
     * @param data The state data
     * @param parentComponent The parent component
     * @param saveToFile Indicates the state needs to save to a file.
     * @param saveToApp Indicates the state needs to save to the application.
     * @param saveTos The save to checkboxes
     * @param exporterMap The exporter map
     * @return {@code true} if the dialog should be shown again.
     */
    private boolean doSaveState(StateData data, Component parentComponent, boolean saveToFile, boolean saveToApp,
            List<JCheckBox> saveTos, Map<JCheckBox, Exporter> exporterMap)
    {
        OutputStream outputStream;
        if (saveToFile)
        {
            MnemonicFileChooser chooser = new MnemonicFileChooser(myPrefsRegistry, getClass().getName());
            chooser.setSelectedFile(new File(data.getId() + "_state.xml"));
            while (true)
            {
                int result = chooser.showSaveDialog(parentComponent, Collections.singleton(".xml"));
                if (result == JFileChooser.APPROVE_OPTION)
                {
                    File saveFile = chooser.getSelectedFile();
                    try
                    {
                        outputStream = new FileOutputStream(saveFile);
                        break;
                    }
                    catch (FileNotFoundException e)
                    {
                        LOGGER.error("Failed to write to selected file [" + saveFile + "]: " + e, e);
                        JOptionPane.showMessageDialog(parentComponent, "Failed to write to file: " + e.getMessage(),
                                "Error writing to file", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else
                {
                    outputStream = null;
                    break;
                }
            }
        }
        else
        {
            outputStream = null;
        }

        if (saveToFile && outputStream == null)
        {
            return true;
        }

        List<Exporter> selectedExporters = new CopyOnWriteArrayList<>(saveTos.stream().filter(cb -> cb.isSelected())
                .map(cb -> exporterMap.get(cb)).filter(e -> e != null).collect(Collectors.toList()));
        OutputStream fos = outputStream;
        EventQueueUtilities.waitCursorRun(parentComponent, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    myController.saveState(data.getId(), data.getDescription(), data.getTags(), data.getModules(), saveToApp,
                            fos);
                    for (Exporter exporter : selectedExporters)
                    {
                        doExportState(parentComponent, exporter, data);
                    }
                }
                finally
                {
                    if (fos != null)
                    {
                        try
                        {
                            fos.close();
                        }
                        catch (IOException e)
                        {
                            LOGGER.error(e, e);
                        }
                    }
                }
            }
        });

        return false;
    }

    /**
     * Does the state export.
     *
     * @param parentComponent the parent component
     * @param exporter the exporter
     * @param data the data
     */
    private void doExportState(Component parentComponent, Exporter exporter, StateData data)
    {
        try
        {
            exporter.setObjects(Collections.singletonList(data));
            exporter.export((File)null);
        }
        catch (IOException | ExportException e)
        {
            String message = "Failed to export state data: " + e.getMessage();
            LOGGER.error(message);
            EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(parentComponent, message, "Error exporting state",
                    JOptionPane.ERROR_MESSAGE));
        }
    }

    /**
     * Disable states action.
     */
    private final class DisableStatesAction extends AbstractAction
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public DisableStatesAction()
        {
            super("Disable States");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Quantify.collectMetric("mist3d.state.disable-states");
            myController.deactivateAllStates();
        }
    }

    /**
     * Delete state action.
     */
    private final class DeleteStatesAction extends AbstractAction
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public DeleteStatesAction()
        {
            super("Delete States");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Quantify.collectMetric("mist3d.state.delete-states");
            Collection<? extends String> availableStates = myController.getAvailableStates();
            if (availableStates.isEmpty())
            {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(myStateControlButton),
                        "There are no states to delete.");
                return;
            }

            DefaultListModel<String> dataModel = new DefaultListModel<>();
            for (String id : availableStates)
            {
                dataModel.addElement(id);
            }
            JList<String> list = new JList<>(dataModel);
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list.setSelectedIndex(0);

            JScrollPane scrollPane = new JScrollPane(list);
            scrollPane.setBorder(BorderFactory.createEtchedBorder());
            OptionDialog dialog = new OptionDialog(SwingUtilities.getWindowAncestor(myStateControlButton), scrollPane);
            dialog.setTitle("Delete States");
            dialog.setPreferredSize(new Dimension(300, 300));
            dialog.buildAndShow();

            if (dialog.getSelection() == JOptionPane.OK_OPTION)
            {
                Collection<String> stateIds = list.getSelectedValuesList();
                if (!stateIds.isEmpty())
                {
                    myController.removeStates(stateIds);
                }
            }
        }
    }

    /**
     * Save state action.
     */
    private final class SaveStateAction extends AbstractAction
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public SaveStateAction()
        {
            super("Save State");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Quantify.collectMetric("mist3d.state.save-state");
            saveState();
        }
    }

    /**
     * Save state action.
     */
    private final class ToggleStateAction extends AbstractAction
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param stateName The name of the state.
         */
        public ToggleStateAction(String stateName)
        {
            super(stateName);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            boolean activeState = myController.isStateActive(getName());
            Quantify.collectMetric("mist3d.state." + (activeState ? "deactivate" : "activate") + "-state");
            CancellableTaskActivity ta = new CancellableTaskActivity();
            ta.setLabelValue((activeState ? "Deactivating state " : "Activating state ") + getName());
            ta.setActive(true);
            myMenuBarRegistry.addTaskActivity(ta);
            SwingWorker<Void, Void> worker = EventQueueUtilities.waitCursorRun(myStateControlButton,
                    () -> myController.toggleState(getName()), () -> ta.setComplete(true));
            ta.cancelledProperty().addListener((v, o, n) -> worker.cancel(true));
        }

        @Override
        public Object getValue(String key)
        {
            if (Action.SELECTED_KEY.equals(key))
            {
                return Boolean.valueOf(myController.isStateActive(getName()));
            }
            else
            {
                return super.getValue(key);
            }
        }

        /**
         * Get the name of the action.
         *
         * @return The name.
         */
        private String getName()
        {
            return (String)getValue(Action.NAME);
        }
    }
}
