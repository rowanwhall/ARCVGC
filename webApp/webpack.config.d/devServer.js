config.devServer = config.devServer || {};
config.devServer.historyApiFallback = true;
config.devServer.proxy = [
    {
        context: ['/api', '/static'],
        target: 'https://arcvgc.com',
        changeOrigin: true,
    }
];
