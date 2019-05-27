package Allocator;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.bind.v2.TODO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

public class AllocatorView extends JFrame {
    MTable table = null;
    RequestTable requestTable = null;
    //0 表示首次适应算法， 1 表示最佳适应算法
    int algorithm = 0;
    Allocator allocator = null;

    // TODO: 做个显示请求的table

    public AllocatorView(){
        super();
        setSize(750, 750);
        //Allocator
//        allocator = new Allocator()

        // 单选框
        JRadioButton[] radioButtons = {
                new JRadioButton("首次适应", true),
                new JRadioButton("最佳适应"),
        };

        JPanel radioPanel=new JPanel();
        radioPanel.setBounds(500,30,200,50);//宽度不够自动换行
        this.add(radioPanel);
        ButtonGroup bg = new ButtonGroup();

        radioPanel.add(radioButtons[0], BorderLayout.WEST);
        radioPanel.add(radioButtons[1], BorderLayout.EAST);

        for(int i = 0;i<radioButtons.length;i++){
            bg.add(radioButtons[i]);
//            radioPanel.add(radioButtons[i]);
            final int index = i;
            radioButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    algorithm = index;
                    resetAll();
                    System.out.println(index);
                }
            });
        }

        //启动按钮和重置
        JButton[] buttons = {
                new JButton("单步执行"),
                new JButton("执行到底"),
                new JButton("重置"),
        };
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(500,80,200,80);
//        buttonPanel.setLayout(null);
        this.add(buttonPanel);

//        for(int i = 0;i<buttons.length;i++){
////            buttonPanel.add(buttons[i]);
//            buttons[i].setSize(80, 20);
//        }
        buttonPanel.add(buttons[0], BorderLayout.WEST);
        buttonPanel.add(buttons[1], BorderLayout.WEST);
        buttonPanel.add(buttons[2]);
        //给按钮加监听器
        buttons[0].addActionListener(actionEvent -> {
            singleStep();
        });
        buttons[1].addActionListener(actionEvent -> {
            runAll();
        });
        buttons[2].addActionListener(actionEvent -> {
            resetAll();
        });


        //显示作业请求的Table
        requestTable = new RequestTable();
//        requestTable.setBounds(0, 0, 250, 200);
//        requestTable.setSize(250, 200);
        requestTable.setPreferredScrollableViewportSize(new Dimension(150, 200));//设置Table的默认大小...
        requestTable.setFillsViewportHeight(true);

        JPanel requestPanel = new JPanel(new FlowLayout());
        requestPanel.setBounds(500, 200, 250, 200);

        requestPanel.add(requestTable);
        requestPanel.add(new JScrollPane(requestTable));//滑动条
        this.add(requestPanel);

        //显示空闲块的Table
        table = new MTable();
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setFillsViewportHeight(true);
        JScrollPane jsp = new JScrollPane(table);
//        table.setPreferredSize(30, 150);
        this.add(table);
        this.add(new JScrollPane(table));//滑动条

        //
        resetAll();

        this.setVisible(true);
//        this.setResizable(false);
        setLocationRelativeTo(null);
//        pack();
    }

    //
    //Listeners
    //Reset
    void resetAll(){
        //重置分配器
        allocator = new Allocator(algorithm == 0? (new firstFit()):(new bestFit()));
        //重置显示内存块的Table
        table.updateBlocks(allocator.algorithm.getEmptyList(),allocator.algorithm.getBusyList());
        // 重置作业请求Table
        requestTable.setRequests(Allocator.getRequests());
    }
    //Single Step
    void singleStep(){
        String request = requestTable.removeOneRow();
        if(request == ""){
            System.out.println("Empty Request!");
            return;
        }
        //TODO: 处理请求
        //分割字符串，转化为具体的请求
        String[] result = request.substring(4, request.length() - 1).split(" ", 4);
        System.out.println(result[0]+" "+result[1]+" "+result[2]);
        int jobIndex = Integer.parseInt(result[0]);
        int blockSize = Integer.parseInt(result[2]);
        if(result[1].equals("Release")){
            allocator.free(jobIndex, blockSize);
        }else if(result[1].equals("Request")){
            allocator.malloc(jobIndex, blockSize);
        }else {
            assert false;
        }

        //更新表格数据
        table.updateBlocks(allocator.algorithm.getEmptyList(),allocator.algorithm.getBusyList());
    }
    //Run till the end
    void runAll(){
        while (requestTable.hasRequest()){
            singleStep();
        }
    }
    //TODO:考虑做个多步执行之类的
}

class BaseTable extends JTable{
    protected void setColomnWidth(){
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);// 设置表格列宽
        for (int i = 0; i < this.getColumnCount(); i++) {
            TableColumn column = getColumnModel().getColumn(i);
            column.setPreferredWidth(150);
        }
    }
    BaseTable(){
        super();
    }
}

class MTable extends BaseTable{
    private DefaultTableModel model = null;
    private final static String[] attrs = {"开始地址", "块大小", "作业编号"};
    private DefaultTableCellRenderer ter = null;

    MTable(){
        super();
        setLayout(null);

        String[][] blocks = {
//                {"0", "640KB", "0"},
//                {"0", "640KB", "0"},
//                {"0", "640KB", "0"},
        };
        model = new DefaultTableModel(blocks, attrs);
        this.setModel(model);


        //设置列宽和行宽
        setRowHeight(25);// 设置表格行宽

        //设置间隔色
        ter = new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                if (row % 2 == 0)
                    setBackground(Color.pink);
                else if (row % 2 == 1)
                    setBackground(Color.white);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        setColumnConfig();
    }

    void setColumnConfig(){
        super.setColomnWidth();


        for (int i = 0; i < attrs.length; i++) {
            this.getColumn(attrs[i]).setCellRenderer(ter);
        }
    }

    //更新空闲链表的显示
    public void updateBlocks(@NotNull List<Block> emptyList, @NotNull List<Block> busyList){
        assert emptyList != null;
        model.setRowCount(0);
        addBlockList(emptyList);//显示空闲链表
        addBlockList(busyList);//显示已分配链表
        setColumnConfig();
    }

    void addBlockList(List<Block> blockList){
        if(blockList==null){
            System.out.println("???");
        }
//        System.out.println(blockList.size());
        Iterator<Block> it = blockList.iterator();
        while (it.hasNext()){
            Block record = it.next();
            if(record.startAddr<0||record.blockSize<=0){
                continue;//处理哨兵节点
            }
//            System.out.println(""+record.startAddr+ ""+record.blockSize);
            model.addRow(new String[]{""+record.startAddr, ""+record.blockSize, ""+record.jobIndex});
        }
    }

}

// 显示请求（申请/释放内存）的表格
class RequestTable extends BaseTable{
    private DefaultTableModel model = null;
    String [] attrs = {"请求"};

    String[][] requests = {
//            {"Job 1 release 100K"},
//            {"Job 1 release 100K"},
//            {"Job 1 release 100K"},
    };
    RequestTable(){
        super();
        setLayout(null);
        model = new DefaultTableModel(requests, attrs);
        this.setModel(model);

        setRowHeight(25);// 设置表格行宽
        super.setColomnWidth();
    }

    //设置列宽


    //重置请求
    public void setRequests(String[][] requests){
        model = new DefaultTableModel(requests,attrs);
        this.setModel(model);
        setColomnWidth();
    }

    //在UI层面删去一行，并且返回第一个Request
    public String removeOneRow(){
        if(model.getRowCount() == 0){
            return "";
        }

        String ret = (String) model.getValueAt(0,0);
//        System.out.println(ret);
        model.removeRow(0);
        return ret;
    }

    public boolean hasRequest(){
        return model.getRowCount() != 0;
    }
}
