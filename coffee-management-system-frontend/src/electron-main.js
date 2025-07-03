const { app, BrowserWindow } = require('electron');
const path = require('path');
const express = require('express');
const fs = require('fs');
const { networkInterfaces } = require('os');

let win;
let server;

function getLocalIP() {
  const nets = networkInterfaces();
  for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
      if (net.family === 'IPv4' && !net.internal) {
        return net.address;
      }
    }
  }
  return 'localhost';
}

function createServer() {
  const expressApp = express();
  
  // 修复路径解析 - 支持开发和打包环境
  let distPath;
  
  if (app.isPackaged) {
    // 打包后的环境
    distPath = path.join(process.resourcesPath, 'dist');
  } else {
    // 开发环境
    const projectRoot = path.resolve(__dirname, '..');
    distPath = path.join(projectRoot, 'dist');
  }
  
  console.log('当前环境:', app.isPackaged ? '打包环境' : '开发环境');
  console.log('dist 路径:', distPath);
  console.log('dist 目录是否存在:', fs.existsSync(distPath));
  
  if (!fs.existsSync(distPath)) {
    console.error('dist 目录不存在！请先运行 npm run build');
    return null;
  }
  
  // 设置正确的MIME类型
  expressApp.use((req, res, next) => {
    if (req.path.endsWith('.js')) {
      res.type('application/javascript');
    } else if (req.path.endsWith('.css')) {
      res.type('text/css');
    }
    next();
  });
  
  // 服务静态文件 - 优先处理
  expressApp.use(express.static(distPath, {
    maxAge: '1d', // 缓存静态资源
    etag: false
  }));
  
  // SPA 路由处理 - 修复版本
  expressApp.get('*', (req, res) => {
    // 检查是否是静态资源请求
    if (req.path.match(/\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$/)) {
      // 如果是静态资源但没有被 express.static 处理到，说明文件不存在
      return res.status(404).send('Static file not found');
    }
    
    // 对于所有页面路由请求，返回 index.html
    const indexPath = path.join(distPath, 'index.html');
    if (fs.existsSync(indexPath)) {
      res.sendFile(indexPath);
    } else {
      res.status(404).send('index.html not found');
    }
  });
  
  const port = 5173;
  server = expressApp.listen(port, '0.0.0.0', () => {
    const localIP = getLocalIP();
    console.log(`应用已启动:`);
    console.log(`本地访问: http://localhost:${port}`);
    console.log(`局域网访问: http://${localIP}:${port}`);
    console.log(`客人点餐页面: http://${localIP}:${port}/customer/1`);
  });
  
  return `http://localhost:${port}`;
}

function createWindow() {
  // 先启动HTTP服务器
  const serverUrl = createServer();
  
  if (!serverUrl) {
    console.error('服务器启动失败');
    return;
  }
  
  win = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      webSecurity: false // 允许跨域请求
    },
  });

  // 加载HTTP服务器地址而不是文件路径
  win.loadURL(serverUrl);

  win.on('closed', () => {
    if (server) {
      server.close();
    }
    win = null;
  });
}

app.on('ready', createWindow);

app.on('window-all-closed', () => {
  if (server) {
    server.close();
  }
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (win === null) {
    createWindow();
  }
});