package com.offlineprogrammer.braintrainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class IAPDataSource {
    private static final String TAG = "IAPDataSource";
    public static enum PurchaseStatus {
        PAID, FULFILLED, UNAVAILABLE, UNKNOWN
    }

    private SQLiteDatabase database;
    private final IAPSQLLiteHelper dbHelper;

    private final String[] allColumns = {
            IAPSQLLiteHelper.COLUMN_RECEIPT_ID,
            IAPSQLLiteHelper.COLUMN_USER_ID,
            IAPSQLLiteHelper.COLUMN_STATUS,
            IAPSQLLiteHelper.COLUMN_SKU,
            IAPSQLLiteHelper.COLUMN_PURCHASE_DATE,
            IAPSQLLiteHelper.COLUMN_CANCEL_DATE};

    public IAPDataSource(final Context context) {
        dbHelper = new IAPSQLLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();

    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Create the purchase record in sqlite database
     *
     * @param receiptId
     *            amazon's receipt id
     * @param userId
     *            amazon user id
     * @param status
     *            the status for the purchase
     */
    public void createPurchase(final String receiptId,
                               final String userId,
                               final PurchaseStatus status,
                               final String sku,
                               final long purchaseDate,
                               final long cancelDate ) {
        Log.d(TAG, "createPurchase: receiptId (" + receiptId + "),userId (" + userId + "), status (" + status + ")");

        final ContentValues values = new ContentValues();
        values.put(IAPSQLLiteHelper.COLUMN_RECEIPT_ID, receiptId);
        values.put(IAPSQLLiteHelper.COLUMN_USER_ID, userId);
        values.put(IAPSQLLiteHelper.COLUMN_STATUS, status.toString());
        values.put(IAPSQLLiteHelper.COLUMN_SKU, sku);
        values.put(IAPSQLLiteHelper.COLUMN_PURCHASE_DATE, purchaseDate);
        values.put(IAPSQLLiteHelper.COLUMN_CANCEL_DATE, cancelDate);
        try {
            database.insertOrThrow(IAPSQLLiteHelper.TABLE_PURCHASES, null, values);
        } catch (final SQLException e) {
            Log.w(TAG, "A purchase with given receipt id already exists, simply discard the new purchase record");
        }
    }

    private PurchaseRecord cursorToPurchaseRecord(final Cursor cursor) {
        final PurchaseRecord purchaseRecord = new PurchaseRecord();
        purchaseRecord.setReceiptId(cursor.getString(cursor.getColumnIndex(IAPSQLLiteHelper.COLUMN_RECEIPT_ID)));
        purchaseRecord.setUserId(cursor.getString(cursor.getColumnIndex(IAPSQLLiteHelper.COLUMN_USER_ID)));
        try {
            purchaseRecord.setStatus(PurchaseStatus.valueOf(cursor.getString(cursor.getColumnIndex(IAPSQLLiteHelper.COLUMN_STATUS))));
        } catch (final Exception e) {
            purchaseRecord.setStatus(PurchaseStatus.UNKNOWN);
        }
        return purchaseRecord;
    }

    /**
     * Return the purchase record by receipt id
     *
     * @param receiptId
     *            amazon receipt id
     * @param userId
     *            user id used to verify the purchase record
     * @return
     */
    public final PurchaseRecord getPurchaseRecord(final String receiptId, final String userId) {
        Log.d(TAG, "getPurchaseRecord: receiptId (" + receiptId + "), userId (" + userId + ")");

        final String where = IAPSQLLiteHelper.COLUMN_RECEIPT_ID + " = ?";
        final Cursor cursor = database.query(IAPSQLLiteHelper.TABLE_PURCHASES,
                allColumns,
                where,
                new String[] { receiptId },
                null,
                null,
                null);
        cursor.moveToFirst();
        // no record found for the given receipt id
        if (cursor.isAfterLast()) {
            Log.d(TAG, "getPurchaseRecord: no record found for receipt id (" + receiptId + ")");
            cursor.close();
            return null;
        }
        final PurchaseRecord purchaseRecord = cursorToPurchaseRecord(cursor);
        cursor.close();
        if (purchaseRecord.getUserId() != null && purchaseRecord.getUserId().equalsIgnoreCase(userId)) {
            Log.d(TAG, "getPurchaseRecord: record found for receipt id (" + receiptId + ")");
            return purchaseRecord;
        } else {
            Log.d(TAG, "getPurchaseRecord: user id not match, receipt id (" + receiptId + "), userId (" + userId + ")");
            // cannot verify the purchase is for the correct user;
            return null;
        }

    }

    /**
     * Update Purchase status for given receipt id
     *
     * @param receiptId
     *            receipt id to update
     * @param fromStatus
     *            current status for the purchase record in table
     * @param toStatus
     *            latest status for the purchase record
     * @return
     */
    public boolean updatePurchaseStatus(final String receiptId,
                                        final PurchaseStatus fromStatus,
                                        final PurchaseStatus toStatus) {
        Log.d(TAG, "updatePurchaseStatus: receiptId (" + receiptId + "), status:(" + fromStatus + "->" + toStatus + ")");

        String where = IAPSQLLiteHelper.COLUMN_RECEIPT_ID + " = ?";
        String[] whereArgs = new String[] { receiptId };

        if (fromStatus != null) {
            where = where + " and " + IAPSQLLiteHelper.COLUMN_STATUS + " = ?";
            whereArgs = new String[] { receiptId, fromStatus.toString() };
        }
        final ContentValues values = new ContentValues();
        values.put(IAPSQLLiteHelper.COLUMN_STATUS, toStatus.toString());
        final int updated = database.update(IAPSQLLiteHelper.TABLE_PURCHASES, values, where, whereArgs);
        Log.d(TAG, "updatePurchaseStatus: updated " + updated);
        return updated > 0;

    }
}


