import axios from "axios";
import { ElMessage } from 'element-plus'

const authItemName = "access_token"

const defaultFailure  = (message,code,url) => {
    console.warn(`请求地址: ${url}, 状态码: ${code}, 错误信息:${message}`)
    ElMessage.warning(message)
}

const defaultError  = (err) => {
    console.error(err)
    ElMessage.warning("发生一些错误，请联系管理员")
}

//
function accessHeader(){
    const token = takeAccessToken()
    return token ? {
        'Authorization': `Bearer ${takeAccessToken()}`
    } : {}
}



function takeAccessToken(){
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName)
    if(!str) return null
    const authObject = JSON.parse(str)
    if(authObject.expire <= new Date()){
        deleteAccessToken()
        ElMessage.warning("登录状态已过期，请重新登录")
        return null
    }
    return authObject.token
}

function deleteAccessToken(){
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}

//把token存储起来
function storeAccessToken(token,expire,remember){
    const authObject = {token:token,expire:expire}
    const str = JSON.stringify(authObject)
    //根据是否勾选记住我，选择token的存储方式
    if(remember)
        localStorage.setItem(authItemName,str)
    else
        sessionStorage.setItem(authItemName,str)
}

function internalPost(url,data,header,success,failure,error= defaultError){
    axios.post(url, data,{ headers:header }).then(({data})=>{
        if(data.code === 200){
            success(data.data)
        }else{
            failure(data.message,data.code,url)
        }
    }).catch(err => error(err))
}

function internalGet(url,header,success,failure,error = defaultError){
    axios.get(url,{ headers:header }).then(({data})=>{
        if(data.code === 200){
            success(data.data)
        }else{
            failure(data.message,data.code,url)
        }
    }).catch(err => error(err))
}

function get(url,success,failure = defaultFailure){
    internalGet(url,accessHeader(),success,failure)
}

function post(url,data,success,failure =  defaultFailure){
    internalPost(url,data,accessHeader(),success,failure)
}

function login(username,password,remember,success,failure = defaultFailure){
    internalPost('/api/auth/login',{
        username: username,
        password: password
    },{
        'Content-Type': 'application/x-www-form-urlencoded'
    },(data)=>{
        storeAccessToken(data.token,data.expire,remember)
        ElMessage.success(`登录成功，欢迎${data.username}来到我们的系统`)
        success(data)
    },failure)
}

function logout(success, failure = defaultFailure){
    get('/api/auth/logout',() => {
        deleteAccessToken();
        ElMessage.success("退出登录成功，欢迎您再次使用")
        success()
    },failure)
}

function unauthorized(){
    return !takeAccessToken()
}

export {login,logout,get,post,unauthorized,accessHeader}