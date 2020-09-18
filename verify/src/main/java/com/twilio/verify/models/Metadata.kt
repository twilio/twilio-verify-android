package com.twilio.verify.models

/**
 * Describes the **Metadata** of a paginated service
 */
interface Metadata {
  /**
   * Current Page
   */
  val page: Int
  /**
   * Number of result per page
   */
  val pageSize: Int
  /**
   * Identifies the previous page
   */
  val previousPageToken: String?
  /**
   * Identifies the next page
   */
  val nextPageToken: String?
}
