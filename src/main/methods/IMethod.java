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
     * n-1 -> 1 预测
     *
     * @throws Exception
     */
    public void predict() throws Exception;

    /**
     * 1 -> 1 预测
     * @throws Exception
     */
    public void predictWithLimitedTrainingSet() throws Exception;
}
