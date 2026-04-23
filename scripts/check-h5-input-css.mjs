import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const cssPath = resolve('src/styles/common.css')
const css = readFileSync(cssPath, 'utf8')
const cssWithoutComments = css.replace(/\/\*[\s\S]*?\*\//g, '')

const failures = []

function normalize(text) {
  return text.replace(/\s+/g, ' ').trim()
}

function findRule(selector, label = selector) {
  const normalizedSelector = normalize(selector)
  const rules = [...cssWithoutComments.matchAll(/([^{}]+)\{([^{}]*)\}/g)]
  const match = rules.find((rule) => normalize(rule[1]) === normalizedSelector)
  if (!match) {
    failures.push(`Missing CSS rule: ${label}`)
    return ''
  }
  return match[2]
}

function requireInRule(rule, needle, label) {
  if (!rule.includes(needle)) {
    failures.push(`${label} must include "${needle}"`)
  }
}

const nativeTextInputRule = findRule('input, textarea')
if (/\bposition\s*:/.test(nativeTextInputRule) || /\bz-index\s*:/.test(nativeTextInputRule)) {
  failures.push('Do not put position or z-index on raw input/textarea; it breaks uni-app H5 native input sizing.')
}

const fieldInputRule = findRule('.field-input')
requireInRule(fieldInputRule, 'height: 88rpx', '.field-input')
requireInRule(fieldInputRule, 'min-height: 88rpx', '.field-input')

const inputInnerRule = findRule(`
  .field-input .uni-input-wrapper,
  .field-input .uni-input-form,
  .field-input .uni-input-input
`, '.field-input uni input internals')
requireInRule(inputInnerRule, 'min-height: 0', '.field-input uni input internals')
requireInRule(inputInnerRule, 'height: 100%', '.field-input uni input internals')

const textareaRule = findRule('.field-textarea')
requireInRule(textareaRule, 'height: 180rpx', '.field-textarea')
requireInRule(textareaRule, 'min-height: 180rpx', '.field-textarea')

const textareaInnerRule = findRule(`
  .field-textarea .uni-textarea-wrapper,
  .field-textarea .uni-textarea-textarea
`, '.field-textarea uni textarea internals')
requireInRule(textareaInnerRule, 'height: 100%', '.field-textarea uni textarea internals')

if (failures.length) {
  console.error('H5 input CSS regression check failed:')
  for (const failure of failures) {
    console.error(`- ${failure}`)
  }
  console.error(`Checked ${cssPath}`)
  process.exit(1)
}

console.log(`H5 input CSS regression check passed: ${normalize(cssPath)}`)
