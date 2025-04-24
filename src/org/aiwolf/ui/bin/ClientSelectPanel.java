package org.aiwolf.ui.bin;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.ui.util.AgentLibraryReader;

public class ClientSelectPanel extends JPanel implements ListSelectionListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	static public void main(String[] args) throws IOException{
//		JFrame frame = new JFrame();
//		frame.setLayout(new BorderLayout());
//		ClientSelectPanel candidatePanel = new ClientSelectPanel();
//		frame.add(candidatePanel);
//		frame.pack();
//		frame.setVisible(true);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		
//		candidatePanel.addJarFile(new File("./jar/gaugau.jar"));
//	
//	}
	
	
	final static private Role[] roleAry = {
		Role.VILLAGER,
		Role.SEER,
		Role.MEDIUM,
		Role.BODYGUARD,
		Role.WEREWOLF,
		Role.POSSESSED,
	};
	

	
	Map<File, List<Class>> jarClassMap;
	
	JList<Item<File>> jarList;
	DefaultListModel<Item<File>> jarModel;

	JList<Item<Class>> classList;
	DefaultListModel<Item<Class>> classModel;
	JTextField classNameField;

	JList<Item<Role>> roleList;
//	private List<URL> libraryList;
	
	
	JButton openFileButton;
	JButton removeFileButton;


	public ClientSelectPanel(){
		Font font = new Font("Meiryo", Font.PLAIN, 16);
		this.setFont(font);
		setBorder(new LineBorder(Color.BLACK));
		setSize(600, 150);
//		setMaximumSize(new Dimension(500, 100));
//		setMinimumSize(new Dimension(500, 100));
		jarClassMap = new LinkedHashMap<>();

		Vector<Item<Role>> roleItemVector = new Vector<>();
		for(Role role:roleAry){
			roleItemVector.add(new Item<Role>(role.toString(), role));
		}
		roleItemVector.add(new Item<Role>("No Request", null));

		jarModel = new DefaultListModel<>();
		classModel = new DefaultListModel<>();
		
		
		//Graphic Initialize
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
//		setLayout(new FlowLayout());
		
		jarList = new JList<Item<File>>(jarModel);
		jarList.setSize(250, 200);
		jarList.setFixedCellWidth(250);
		jarList.setAutoscrolls(true);
		JScrollPane jarScrollPane = new JScrollPane(jarList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jarScrollPane.setSize(300,200);
		jarList.addListSelectionListener(this);
//		jarList.setBorder(new LineBorder(Color.BLUE));

		openFileButton = new JButton("Open File");
		removeFileButton = new JButton("Remove");
		
		JPanel spanel = new JPanel();
		spanel.setLayout(new BoxLayout(spanel, BoxLayout.Y_AXIS));
//		spanel.add(new JLabel("Jar Files", SwingConstants.LEFT));
		spanel.add(jarScrollPane);
//		spanel.setBorder(new LineBorder(Color.BLACK));
		JPanel bpanel = new JPanel();
		bpanel.setLayout(new FlowLayout());
		bpanel.add(openFileButton);
		openFileButton.addActionListener(this);
		bpanel.add(removeFileButton);
		removeFileButton.addActionListener(this);
		spanel.add(bpanel);
		spanel.setBorder(new TitledBorder(new EmptyBorder(1,1,1,1), "JarFiles"));
		add(spanel);
		
		///////////////////
		classList = new JList<Item<Class>>(classModel);
		classList.setSize(250, 200);
		classList.setFixedCellWidth(250);
		classList.addListSelectionListener(this);
		JScrollPane classScrollPane = new JScrollPane(classList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		classScrollPane.setSize(300,200);
		JPanel cpanel = new JPanel();
		cpanel.setLayout(new BoxLayout(cpanel, BoxLayout.Y_AXIS));
		cpanel.setBorder(new TitledBorder(new EmptyBorder(1,1,1,1), "Class"));
		cpanel.add(classScrollPane);
		cpanel.setMinimumSize(spanel.getSize());
		classNameField = new JTextField();
		classNameField.setEditable(false);
		cpanel.add(classNameField);
		add(cpanel);
		
		roleList = new JList<>(roleItemVector);
		roleList.setSize(100, 200);
		roleList.setFixedCellWidth(100);
		JScrollPane roleScrollPane = new JScrollPane(roleList);
//		roleList.setFixedCellHeight(20);
		JPanel rpanel = new JPanel();
		rpanel.add(roleScrollPane);
		rpanel.setBorder(new TitledBorder(new EmptyBorder(1,1,1,1), "Role"));
		add(rpanel);

		
//		libraryList = new ArrayList<>();
//		setTransferHandler(new DropFileHandler(libraryList));
		setTransferHandler(new DropFileHandler());
	}
	
	public void addJarFile(File jarFile) throws IOException{
		List<Class> classList = AgentLibraryReader.getPlayerClassList(jarFile);
		if(jarClassMap.containsKey(jarFile)){
			return;
		}
		jarClassMap.put(jarFile, classList);

		jarModel.addElement(new Item<File>(jarFile.getName(), jarFile));
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getSource() == jarList){
			classModel.clear();
			Item<File> selectedItem = jarList.getSelectedValue();
			if(selectedItem != null){
				List<Class> list = jarClassMap.get(selectedItem.getItem());
				if(list != null){
					for(Class cls:list){
						try {
							Player player = (Player) cls.newInstance();
							if(player.getName() == null){
								classModel.addElement(new Item(cls.getSimpleName(), cls));
							}
							else{
								classModel.addElement(new Item(player.getName(), cls));
							}
						} catch (InstantiationException e1) {
							// TODO 自動生成された catch ブロック
							e1.printStackTrace();
						} catch (IllegalAccessException e1) {
							// TODO 自動生成された catch ブロック
							e1.printStackTrace();
						}
						
					}
				}
			}
		}
		else if(e.getSource() == classList){
			Item<Class> selectedItem = classList.getSelectedValue();
			if(selectedItem != null){
				classNameField.setText(selectedItem.getItem().getName());
			}
			else{
				classNameField.setText("");
			}
		}
	}


	
	
	public File getSelectedJarFile(){
		try{
			return jarList.getSelectedValue().getItem();
		}catch(NullPointerException e){
			return null;
		}
	}
	
	public Class getSelectedClass(){
		try{
			return classList.getSelectedValue().getItem();
		}catch(NullPointerException e){
			return null;
		}
	}
	
	public Role getSelectedRole(){
		try{
			return roleList.getSelectedValue().getItem();
		}catch(NullPointerException e){
			return null;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == removeFileButton){
			int idx = jarList.getSelectedIndex();
			if(idx < 0){
				return;
			}
//			System.out.println("Remove "+idx);
			Item<File> removedItem = jarModel.remove(idx);
//			jarList.remove(idx);
			jarClassMap.remove(removedItem.getItem());
		}
		else if (e.getSource() == openFileButton) {
			JFileChooser fileChooser = new JFileChooser(new File("./"));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int selected = fileChooser.showOpenDialog(this);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				if(file.isDirectory()){
					for(File jarFile:AgentLibraryReader.getJarFileList(file)){
						try {
							addJarFile(jarFile);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				else if(file.getName().endsWith("jar")){
					try {
						addJarFile(file);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		
	}

	/**
	 * ドロップ操作の処理を行うクラス
	 */
	private class DropFileHandler extends TransferHandler {
 
//		List<URL> urlList;
		
//		public DropFileHandler(List<URL> urlList) {
//			this.urlList = urlList;
//		}
		
		
		public DropFileHandler() {
		}
		/**
		 * ドロップされたものを受け取るか判断 (ファイルのときだけ受け取る)
		 */
		@Override
		public boolean canImport(TransferSupport support) {
			
			if (!support.isDrop()) {
				// ドロップ操作でない場合は受け取らない
		        return false;
		    }
 
			if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				// ドロップされたのがファイルでない場合は受け取らない
		        return false;
		    }
 
			return true;
		}
 
		/**
		 * ドロップされたファイルを受け取る
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean importData(TransferSupport support) {
			// 受け取っていいものか確認する
			if (!canImport(support)) {
				System.out.println("Can not support");
		        return false;
		    }
 
			// ドロップ処理
			Transferable t = support.getTransferable();
			try {
				// ファイルを受け取る
				List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
 
				for(final File file:files){
					for(File jarFile:AgentLibraryReader.getJarFileList(file)){
						addJarFile(jarFile);
					}
//					urlList.addAll(loadJar(file, true));
				}
			
			
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
//			
//			try {
//				List<Class<? extends Player>> classList = loadPlayers();
//				playerPanel.addPlayerClassList(classList);
//			} catch (IOException e) {
//				// TODO 自動生成された catch ブロック
//				e.printStackTrace();
//			}
			return true;
		}
	}

	
	/**
	 * Get Jar File List
	 * @return
	 */
	public List<File> getJarFileList() {
		List<File> jarFileList = new ArrayList<>();
		for(int i = 0; i < jarModel.size(); i++){
			jarFileList.add(jarModel.getElementAt(i).getItem());
		}
		return jarFileList;
	}


}

class Item<T>{
	String name;
	T item;
	
	
	
	
	public Item(String name, T item) {
		super();
		this.name = name;
		this.item = item;
	}
	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name セットする name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return item
	 */
	public T getItem() {
		return item;
	}
	/**
	 * @param item セットする item
	 */
	public void setItem(T item) {
		this.item = item;
	}
	
	public String toString(){
		return name;
	}
	/* (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	/* (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
}
