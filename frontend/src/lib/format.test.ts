import { describe, expect, it } from 'vitest';
import { bps, money, ms, px } from './format';

describe('format helpers', () => {
  it('formats financial workstation values', () => {
    expect(px(101.1832)).toBe('101.183');
    expect(bps(5.734)).toBe('5.7 bps');
    expect(ms(1200)).toBe('1.2s');
    expect(money(5_000_000, true)).toBe('$5MM');
  });
});
