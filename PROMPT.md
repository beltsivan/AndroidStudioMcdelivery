The manager dashboard now has a per-branch coupon system using Firestore collection `coupons/{autoId}` with fields: code, description, discountType ("fixed" or "percentage"),
discountValue, minOrderAmount, maxUsage, usedCount, expiresAt (Timestamp), isActive, branchId, createdAt.

Implement in the customer mobile app:
1. On checkout, query `coupons` where `branchId == selectedBranchId && isActive == true && expiresAt > now`. Client-side filter out any where `maxUsage > 0 && usedCount >= maxUsage`.
2. Display available coupons as a selectable list (show code, description, discount value, min order requirement).
3. On apply: validate subtotal >= minOrderAmount, compute discount (fixed = discountValue, percentage = subtotal * discountValue / 100), cap at subtotal if over.
4. Submit order with fields: `couponId`, `couponCode`, `discountAmount`, `subtotal`, `deliveryFee`, `total` (subtotal + deliveryFee - discountAmount).
5. After successful order: `updateDoc(doc(db, 'coupons', couponId), { usedCount: increment(1) })`.
6. In order summary, show "Discount (CODE): -₱amount" in green between Subtotal and Delivery Fee.