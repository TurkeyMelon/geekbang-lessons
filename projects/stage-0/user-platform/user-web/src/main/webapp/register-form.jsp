<head>
    <jsp:directive.include file="/WEB-INF/jsp/prelude/include-head-meta.jspf"/>
    <title>User Register</title>
</head>
<body>
<div class="container">
    <h1>注册</h1>
    <form action="${pageContext.request.contextPath}register" method="post">
        <table>
            <tr>
                <td>
                    <label for="inputName">用户名</label>
                    <input type="text" id="inputName" name="name" placeholder="请输入用户名" required autofocus><br>
                </td>
            </tr>
            <tr>
                <td>
                    <label for="inputPassword">密码</label>
                    <input type="password" id="inputPassword" name="password" placeholder="请输入密码" required><br>
                </td>
            </tr>
            <tr>
                <td>
                    <label for="inputEmail">请输出电子邮件</label>
                    <input type="email" id="inputEmail" name="email" placeholder="请输入电子邮件"><br>
                </td>
            </tr>
            <tr>
                <td>
                    <label for="inputPhoneNumber">请输入手机号</label>
                    <input type="text" id="inputPhoneNumber" name="phoneNumber" placeholder="请输入手机号" required autofocus><br>
                </td>
            </tr>
            <tr>
                <td>
                    <button class="btn btn-lg btn-primary btn-block" type="submit" style="width: 100px">注册</button>
                </td>
            </tr>
        </table>
    </form>
</div>
</body>