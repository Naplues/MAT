package main.methods;

/**
 * 一个分类方法应该实现的接口
 */
public interface IMethod {

    /**
     * 准备数据
     */
    public void prepareData();

    /**
     * 预测结果
     *
     * @throws Exception
     */
    public void predict() throws Exception;
}
