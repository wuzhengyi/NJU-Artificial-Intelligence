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

对下列函数加入`alpha`与`beta`参数，进行$\alpha-\beta$剪枝
```java
public float miniMaxRecursor(State state, int depth, boolean maximize, float alpha, float beta) {
    // Has this state already been computed?
    if (computedStates.containsKey(state)) 
                // Return the stored result
                return computedStates.get(state);
    // Is this state done?
    if (state.getStatus() != Status.Ongoing)
                // Store and return
                return finalize(state, state.heuristic());
    // Have we reached the end of the line?
    if (depth == this.depth)
                //Return the heuristic value
                return state.heuristic();
            
    // If not, recurse further. Identify the best actions to take.
    float value = maximize ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
    int flag = maximize ? 1 : -1;
    List<Action> test = state.getActions();
    for (Action action : test) {
        // Check it. Is it better? If so, keep it.
        try {
            State childState = action.applyTo(state);
            float newValue = this.miniMaxRecursor(childState, depth + 1, !maximize, alpha, beta);
            //Record the best value
            if (flag * newValue > flag * value) 
                value = newValue;
            //alpha-beta cut
            if (maximize) {
                if (value >= beta)
                    return value;
                alpha = alpha > value ? alpha : value;
            } else {
                if (value <= alpha)
                    return value;
                beta = beta < value ? beta : value;
            }
        } catch (InvalidActionException e) {
            //Should not go here
            throw new RuntimeException("Invalid action!");
        }
    }
    // Store so we don't have to compute it again.
    return finalize(state, value);
}
```

在`Othello`中递归深度为2时，加入$\alpha-\beta$剪枝后速度几乎毫无变化。
逐渐加深深度，由于深度不大，所以效果依旧不太明显。

最后将`Othello`中递归深度由2改为8
- 在原有未剪枝的版本，前两步还在1s左右，到第三步就默默的不动了。
- 在加入$\alpha-\beta$剪枝后，每一步反应时间大约在1s左右。

可见加入了$\alpha-\beta$剪枝后速度大幅度提升。

### Task 3