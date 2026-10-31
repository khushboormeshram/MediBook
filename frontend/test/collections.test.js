import test from 'node:test'
import assert from 'node:assert/strict'
import {asItems} from '../src/services/collections.js'

test('asItems accepts an unpaged list', () => {
  const doctors = [{id: 3}, {id: 4}]
  assert.deepEqual(asItems(doctors), doctors)
})

test('asItems accepts a paginated API response', () => {
  const doctors = [{id: 3}]
  assert.deepEqual(asItems({content: doctors, totalElements: 1}), doctors)
})

test('asItems safely handles invalid response shapes', () => {
  assert.deepEqual(asItems(null), [])
  assert.deepEqual(asItems({content: null}), [])
})
