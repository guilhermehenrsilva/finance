import { readFileSync } from 'node:fs'
import { spawn } from 'node:child_process'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const env = { ...process.env }

for (const line of readFileSync(resolve(root, '.env'), 'utf8').split(/\r?\n/)) {
  const match = line.match(/^\s*([^#=]+)=(.*)$/)
  if (match) env[match[1].trim()] = match[2]
}

const backend = spawn('mvnw.cmd', ['spring-boot:run'], {
  cwd: root,
  env,
  stdio: 'inherit',
  shell: true,
  windowsHide: false,
})
const frontend = spawn('npm.cmd', ['run', 'dev', '--', '--host', 'localhost'], {
  cwd: resolve(root, 'frontend'),
  env,
  stdio: 'inherit',
  shell: true,
  windowsHide: false,
})

let stopping = false
function stop(exitCode = 0) {
  if (stopping) return
  stopping = true
  if (!backend.killed) backend.kill()
  if (!frontend.killed) frontend.kill()
  process.exitCode = exitCode
}

backend.on('error', (error) => {
  console.error(`Não foi possível iniciar o backend: ${error.message}`)
  stop(1)
})
frontend.on('error', (error) => {
  console.error(`Não foi possível iniciar o frontend: ${error.message}`)
  stop(1)
})
backend.on('exit', (code) => {
  if (!stopping && code !== 0) stop(code ?? 1)
})
frontend.on('exit', (code) => {
  if (!stopping && code !== 0) stop(code ?? 1)
})
process.on('SIGINT', () => stop())
process.on('SIGTERM', () => stop())
