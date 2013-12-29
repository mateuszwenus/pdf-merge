package com.github.mateuszwenus.pdf_merge;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class App {

	private static final String ICON_DELETE = "delete.png";
	private static final String ICON_UP = "up.png";
	private static final String ICON_DOWN = "down.png";
	private static final String ICON_ADD = "add.png";
	private static final String ICON_SAVE = "save.png";

	private JFrame frame;
	private JList filesList;
	private DefaultListModel listModel = new DefaultListModel();
	private JProgressBar progressBar;
	private JButton generateButton;
	private ResourceBundle resourceBundle;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		new App();
	}

	public App() {
		resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());
		frame = new JFrame(resourceBundle.getString("app.title"));
		frame.setLayout(new FlowLayout());
		frame.add(createFramePanel());
		frame.pack();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private JPanel createFramePanel() {
		JPanel framePanel = new JPanel();
		framePanel.setLayout(new BoxLayout(framePanel, BoxLayout.Y_AXIS));
		framePanel.add(createUpPanel());
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setString("");
		framePanel.add(progressBar);
		return framePanel;
	}

	private Component createUpPanel() {
		JPanel upPanel = new JPanel();
		upPanel.setLayout(new FlowLayout());
		upPanel.add(createFilesPanel());
		upPanel.add(createButtonsPanel());
		return upPanel;
	}

	private JScrollPane createFilesPanel() {
		filesList = new JList(listModel);
		filesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		filesList.setLayoutOrientation(JList.VERTICAL);
		filesList.setVisibleRowCount(0);
		listModel.addListDataListener(new ListDataListener() {
			public void intervalRemoved(ListDataEvent e) {
				generateButton.setEnabled(listModel.size() > 0);
			}

			public void intervalAdded(ListDataEvent e) {
				generateButton.setEnabled(listModel.size() > 0);
			}

			public void contentsChanged(ListDataEvent e) {
				generateButton.setEnabled(listModel.size() > 0);
			}
		});
		JScrollPane scrollPane = new JScrollPane(filesList);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		return scrollPane;
	}

	private JPanel createButtonsPanel() {
		JPanel buttonsPane = new JPanel();
		buttonsPane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.ipadx = 10;
		gbc.ipady = 0;
		buttonsPane.add(createAddButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createMoveUpButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createMoveDownButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createDeleteSelectedButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createDeleteAllButton(), gbc);
		gbc.gridy++;
		buttonsPane.add(createGenerateButton(), gbc);
		return buttonsPane;
	}

	private JButton createGenerateButton() {
		generateButton = createButton(resourceBundle.getString("action.generatePdf"), ICON_SAVE);
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				GeneratePdfTask task = new GeneratePdfTask(listModel.toArray(), progressBar, resourceBundle);
				task.execute();
			}
		});
		generateButton.setEnabled(false);
		return generateButton;
	}

	private JButton createMoveDownButton() {
		JButton moveDownButton = createButton(resourceBundle.getString("action.moveDown"), ICON_DOWN);
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] idxArr = filesList.getSelectedIndices();
				reverse(idxArr);
				int[] selIdx = new int[idxArr.length];
				for (int i = 0; i < idxArr.length; i++) {
					if (idxArr[i] < listModel.size() - i - 1) {
						Object itemToMoveDown = listModel.remove(idxArr[i]);
						listModel.add(idxArr[i] + 1, itemToMoveDown);
						selIdx[i] = idxArr[i] + 1;
					} else {
						selIdx[i] = idxArr[i];
					}
				}
				reverse(selIdx);
				filesList.setSelectedIndices(selIdx);
			}
		});
		return moveDownButton;
	}

	protected void reverse(int[] idxArr) {
		for (int i = 0; i < idxArr.length / 2; i++) {
			int tmp = idxArr[i];
			idxArr[i] = idxArr[idxArr.length - i - 1];
			idxArr[idxArr.length - i - 1] = tmp;
		}
	}

	private JButton createMoveUpButton() {
		JButton moveUpButton = createButton(resourceBundle.getString("action.moveUp"), ICON_UP);
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] idxArr = filesList.getSelectedIndices();
				int[] selIdx = new int[idxArr.length];
				for (int i = 0; i < idxArr.length; i++) {
					if (idxArr[i] > i) {
						Object itemToMoveUp = listModel.remove(idxArr[i]);
						listModel.add(idxArr[i] - 1, itemToMoveUp);
						selIdx[i] = idxArr[i] - 1;
					} else {
						selIdx[i] = idxArr[i];
					}
				}
				filesList.setSelectedIndices(selIdx);
			}
		});
		return moveUpButton;
	}

	private JButton createDeleteSelectedButton() {
		JButton deleteSelectedButton = createButton(resourceBundle.getString("action.deleteSelected"), ICON_DELETE);
		deleteSelectedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] idxArr = filesList.getSelectedIndices();
				for (int i = 0; i < idxArr.length; i++) {
					listModel.remove(idxArr[i] - i);
				}
			}
		});
		return deleteSelectedButton;
	}

	private JButton createDeleteAllButton() {
		JButton deleteAllButton = createButton(resourceBundle.getString("action.deleteAll"), ICON_DELETE);
		deleteAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listModel.clear();
			}
		});
		return deleteAllButton;
	}

	private JButton createAddButton() {
		JButton addButton = createButton(resourceBundle.getString("action.addFiles"), ICON_ADD);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF files", "pdf");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					for (File f : chooser.getSelectedFiles()) {
						listModel.addElement(f.getAbsolutePath());
					}
				}

			}
		});
		return addButton;
	}

	private JButton createButton(String text, String iconPath) {
		JButton btn = new JButton(text, createIcon(iconPath));
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		return btn;
	}

	private Icon createIcon(String path) {
		URL imgURL = getClass().getResource("/" + path);
		return new ImageIcon(imgURL);
	}

}
