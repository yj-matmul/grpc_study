// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sample.proto

package sample;

public interface ProductInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:sample.ProductInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  String getId();
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>string name = 2;</code>
   * @return The name.
   */
  String getName();
  /**
   * <code>string name = 2;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string description = 3;</code>
   * @return The description.
   */
  String getDescription();
  /**
   * <code>string description = 3;</code>
   * @return The bytes for description.
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <code>int32 price = 4;</code>
   * @return The price.
   */
  int getPrice();
}
