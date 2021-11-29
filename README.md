### 注意事项
#### 验签字符串的组成
```
appId+appId值+timeStamp+timeStamp值+nonce+nonce值+URL参数排序后的key+value+请求体json字符串+url+appSecret
```

#### URL参数是数组时，拼接的value先后顺序，按照URL中value的先后顺序，中间以逗号分割，前端加签也要按照这种顺序和格式 key=value1,value2,value3
```
http://localhost:8080/v1/get?aa=11&aa=22&aa=33
aa=11,22,33 这种组合方式加签
```

#### 定下规章制度，只追求简洁，禁止过度复杂坑自己。要知道有哪些坑，参数要规范，禁止为了配置参数多样性，写各种if else坑自己后期维护
* 禁止设置server.servlet.context-path取值，否则代码要判断context-path取值，导致多出几个if else