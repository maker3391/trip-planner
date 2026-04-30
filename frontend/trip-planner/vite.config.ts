import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  // 현재 작업 디렉토리(process.cwd())에서 환경 변수를 로드합니다.
  // 세 번째 인자를 ''로 설정하면 VITE_ 접두사가 없는 변수도 로드할 수 있습니다.
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': {
          // 이제 env 객체를 통해 변수에 접근합니다.
          target: env.VITE_API_URL,
          changeOrigin: true,
        }
      }
    }
  }
})