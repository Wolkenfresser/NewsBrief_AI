import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const backend = 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': backend,
      '/actuator': backend,
      '/swagger-ui.html': backend,
      '/swagger-ui': backend,
      '/v3/api-docs': backend,
    },
  },
});
