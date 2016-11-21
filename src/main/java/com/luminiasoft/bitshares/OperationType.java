package com.luminiasoft.bitshares;

/**
 * Created by nelson on 11/6/16.
 */
public enum OperationType {
    transfer_operation,
    limit_order_create_operation,
    limit_order_cancel_operation,
    call_order_update_operation,
    fill_order_operation,           // VIRTUAL
    account_create_operation,
    account_update_operation,
    account_whitelist_operation,
    account_upgrade_operation,
    account_transfer_operation,
    asset_create_operation,
    asset_update_operation,
    asset_update_bitasset_operation,
    asset_update_feed_producers_operation,
    asset_issue_operation,
    asset_reserve_operation,
    asset_fund_fee_pool_operation,
    asset_settle_operation,
    asset_global_settle_operation,
    asset_publish_feed_operation,
    witness_create_operation,
    witness_update_operation,
    proposal_create_operation,
    proposal_update_operation,
    proposal_delete_operation,
    withdraw_permission_create_operation,
    withdraw_permission_update_operation,
    withdraw_permission_claim_operation,
    withdraw_permission_delete_operation,
    committee_member_create_operation,
    committee_member_update_operation,
    committee_member_update_global_parameters_operation,
    vesting_balance_create_operation,
    vesting_balance_withdraw_operation,
    worker_create_operation,
    custom_operation,
    assert_operation,
    balance_claim_operation,
    override_transfer_operation,
    transfer_to_blind_operation,
    blind_transfer_operation,
    transfer_from_blind_operation,
    asset_settle_cancel_operation,  // VIRTUAL
    asset_claim_fees_operation,
    fba_distribute_operation // VIRTUAL
}
