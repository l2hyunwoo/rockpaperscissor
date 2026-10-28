import { spawnSync } from 'node:child_process';

const format = process.argv[2] === '--fix';
if (process.argv.slice(2).some(arg => arg !== '--fix')) throw Error('Use lint-kotlin.mjs [--fix]');
const windows = process.platform === 'win32';
const result = spawnSync(windows ? 'gradlew.bat' : './gradlew', [format ? 'ktlintFormat' : 'ktlintCheck', '--console=plain'], { stdio: 'inherit', shell: windows });
if (result.error) throw result.error;
process.exitCode = result.status ?? 1;
