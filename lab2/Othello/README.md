# 人工智能 实验二

**151220129 计科 吴政亿 18805156360@163.com**

## Task 1

`MiniMaxDecider.java`的代码相比于书本中的样例，将min与max进行了合并，用一个`boolean maximize`代替，以及通过+1,-1将两个函数抽象成一个，下面对三个函数进行简要介绍。

### 变量定义

| 类型      | 变量名            | 意义                         |
|---------|----------------|----------------------------|
| boolean | maximize       | 为true时最大化val,为false时最小化val |
| int     | depth          | 限制了递归的深度                   |
| Map     | computedStates | 用Hash存储不同局面及得分，避免重复运算

### MiniMaxDecider(boolean maximize, int depth)

对上面的变量进行初始化

### public Action decide(State state)

1. 根据maximize将value初始化为负无穷或正无穷
2. 定义`bestAction`用于存储最高得分的动作序列
3. 初始化因子`flag`为-1或+1.
4. 遍历所有当前局势下所有可能的动作
    - 定义变量`newState`与`newValue`为此动作的预期局势与得分
    - 如果`maximize == true`则取其中的得分最高项，反之取最低得分
    - 两个if是为了保证`bestAction`中的actions得分相同且均为最优解
5. 从`bestAction`中随机选取一个action执行

### public float miniMaxRecursor(State state, int depth, boolean maximize)

此函数与上一个函数`decide`相似，是抽象出来的递归部分

1. 如果`computedStates`中存在，即已经计算过得分，则直接返回
2. 如果游戏结束或者到达递归最底层，则直接返回当前局面由启发式函数计算的评估分数
3. 与函数`decide`大体相同
4. 最后返回评估分数

### private float finalize(State state, float value)

未使用，返回`value`。

## Task 2