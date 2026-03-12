config.devServer = config.devServer || {};
config.devServer.proxy = [
    {
        context: ['/api', '/static'],
        target: 'https://arcvgc.com',
        changeOrigin: true,
    }
];
