package org.jflame.commons.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import org.jflame.commons.util.CollectionHelper;

public class TreeNode implements Serializable {

    private static final long serialVersionUID = 2080105591715808494L;
    public static final String STATE_OPEN = "open";
    public static final String STATE_CLOSED = "closed";

    private int id;
    private String label;// 节点名称
    private String iconCls;// 标签图标
    private Integer parentId;// 父节点
    private int level;// 节点级别
    /**
     * 节点状态,打开或关闭.
     * 
     * @see #STATE_CLOSED
     * @see #STATE_OPEN
     */
    private String state;
    /**
     * 节点是否选中
     */
    private Boolean checked;
    /**
     * 子节点
     */
    private List<TreeNode> children = new ArrayList<>();
    /**
     * 附带属性
     */
    private Map<String,Object> attributes = new HashMap<>();

    public TreeNode() {
    }

    public TreeNode(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public TreeNode(int id, String label, int level) {
        this.id = id;
        this.label = label;
        this.level = level;
    }

    public TreeNode(int id, String label, Integer parentId, Integer level) {
        this.id = id;
        this.label = label;
        this.parentId = parentId;
        this.level = level;
    }

    public TreeNode(int id, String label, String iconCls, Integer parentId, Integer level) {
        this.id = id;
        this.label = label;
        this.iconCls = iconCls;
        this.parentId = parentId;
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIconCls() {
        return iconCls;
    }

    public void setIconCls(String iconCls) {
        this.iconCls = iconCls;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public Map<String,Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String,Object> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String attrKey, Object attrValue) {
        this.attributes.put(attrKey, attrValue);
    }

    /**
     * 增加一个子节点
     * 
     * @param child 子节点
     */
    public void addNode(TreeNode child) {
        children.add(child);
        child.setParentId(this.getId());
    }

    /**
     * 增加多个子节点
     * 
     * @param childs 子节点集合
     */
    public void addNodes(Collection<? extends TreeNode> childs) {
        children.addAll(childs);
        for (TreeNode treeNode : childs) {
            treeNode.setParentId(this.getId());
        }
    }

    /**
     * 删除指定索引位置处的子节点
     * 
     * @param index
     * @return 返回删除的子节点,如果索引位置不存在返回null
     */
    public TreeNode removeNode(int index) {
        if (index < 0 || index >= children.size()) {
            return null;
        }
        TreeNode removeNode = children.remove(index);
        removeNode.setParentId(0);
        return removeNode;
    }

    /**
     * 删除子节点
     * 
     * @param node 子节点
     * @return 如存在相等子节点删除并返回true,否则返回false
     */
    public boolean removeNode(TreeNode node) {
        if (children.remove(node)) {
            node.setParentId(0);
            return true;
        }
        return false;
    }

    public boolean hasChild() {
        return CollectionHelper.isNotEmpty(children);
    }

    /**
     * 用给定的节点集合构建树
     * 
     * @param nodes
     * @return
     */
    public static <T extends TreeNode> List<T> buildTree(List<T> nodes) {
        return buildTree(nodes, null);
    }

    /**
     * 用给定的节点集合构建树,并设置选中的节点状态
     * 
     * @param nodes 节点集合
     * @param checkedIds 前端要选中的节点id,不选中可为null
     * @return
     */
    public static <T extends TreeNode> List<T> buildTree(List<T> nodes, int[] checkedIds) {
        List<T> treeGrid = new ArrayList<>();
        TreeNode root = new TreeNode();
        root.setId(0);
        buildTreeNode(root, nodes, checkedIds);
        /*for (T node : funs) {
            if (node.getParentId() == 0) {
                if (ArrayUtils.contains(checkedIds, node.getId())) {
                    node.setChecked(true);
                }
                treeGrid.add(node);
                buildElTreeNode(node, funs, checkedIds);
                node.setState(TreeNode.STATE_OPEN);// 一级展开
            }
        }*/
        return treeGrid;
    }

    private static <T extends TreeNode> void buildTreeNode(TreeNode parent, List<T> nodes, int[] checkedIds) {
        boolean isCheck = ArrayUtils.isEmpty(checkedIds);
        for (T node : nodes) {
            if (node.getParentId() == parent.getId()) {
                if (isCheck && ArrayUtils.contains(checkedIds, node.getId())) {
                    node.setChecked(true);
                }
                parent.setState(node.getParentId() == 0 ? TreeNode.STATE_OPEN : TreeNode.STATE_CLOSED);
                parent.addNode(node);
                buildTreeNode(node, nodes, checkedIds);
            }
        }
    }

}
