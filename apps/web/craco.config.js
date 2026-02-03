const path = require('path');

module.exports = {
  webpack: {
    configure: (webpackConfig) => {
      // Eliminar ModuleScopePlugin para permitir importar fuera de src/
      const ModuleScopePlugin = webpackConfig.resolve.plugins.find(
        (plugin) => plugin.constructor && plugin.constructor.name === 'ModuleScopePlugin'
      );
      if (ModuleScopePlugin) {
        webpackConfig.resolve.plugins = webpackConfig.resolve.plugins.filter(
          (plugin) => plugin !== ModuleScopePlugin
        );
      }

      // Incluir el paquete UI en la transpilación
      const oneOfRule = webpackConfig.module.rules.find((rule) => rule.oneOf);
      if (oneOfRule) {
        const babelRule = oneOfRule.oneOf.find((rule) =>
          rule.loader && rule.loader.includes('babel-loader')
        );

        if (babelRule) {
          // Asegurarse de que include sea un array
          if (!Array.isArray(babelRule.include)) {
            babelRule.include = [babelRule.include];
          }
          babelRule.include.push(path.resolve(__dirname, '../../packages/ui/src'));
        }
      }

      // Resolver alias para usar el directorio src del paquete UI
      webpackConfig.resolve.alias = {
        ...webpackConfig.resolve.alias,
        '@checklist/ui': path.resolve(__dirname, '../../packages/ui/src')
      };

      return webpackConfig;
    }
  }
};
