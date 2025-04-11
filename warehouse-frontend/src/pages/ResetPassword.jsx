import { useState, useEffect } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { Form, Input, Button, message } from "antd";
import axios from "axios";

const ResetPassword = () => {
  const [loading, setLoading] = useState(false);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const token = searchParams.get("token");

  useEffect(() => {
    if (!token) {
      message.error("Token không hợp lệ! Vui lòng thử lại.");
      navigate("/login");
    }
  }, [token, navigate]);

  const handleSubmit = async (values) => {
    setLoading(true);
    try {
      await axios.post("http://localhost:8080/api/reset-password", {
        token,
        newPassword: values.newPassword,
      });

      message.success("Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
      navigate("/login");
    } catch (error) {
      console.error("Reset password error:", error);
      if (error.response) {
        message.error(
          "Lỗi: " +
            (error.response.data.error || "Token không hợp lệ hoặc đã hết hạn!")
        );
      } else {
        message.error("Đã có lỗi xảy ra. Vui lòng thử lại!");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 flex w-screen h-screen">
      <div className="w-1/2 flex flex-col items-center justify-center bg-green-500 text-white">
        <div className="flex flex-col items-center">
          <img
            src="https://cdn-icons-png.flaticon.com/512/456/456212.png"
            alt="User Icon"
            className="w-28 h-28 mb-4"
          />
          <h2 className="text-4xl font-bold">RESET PASSWORD</h2>
        </div>
      </div>

      <div className="w-1/2 flex items-center justify-center bg-gray-900">
        <Form
          form={form}
          onFinish={handleSubmit}
          className="text-white w-96 p-8"
        >
          <h2 className="text-3xl font-semibold text-center mb-6">
            Đặt lại mật khẩu
          </h2>

          <Form.Item
            name="newPassword"
            rules={[{ required: true, message: "Vui lòng nhập mật khẩu mới!" }]}
          >
            <div>
              <label className="block text-gray-300 mb-1">Mật khẩu mới</label>
              <Input.Password
                className="w-full px-4 py-2 rounded-md bg-gray-800 border border-gray-600 text-white focus:outline-none focus:border-green-500 focus:bg-gray-700 transition-colors duration-200 custom-input custom-password-input"
                placeholder="Nhập mật khẩu mới"
              />
            </div>
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            dependencies={["newPassword"]}
            rules={[
              { required: true, message: "Vui lòng xác nhận mật khẩu!" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue("newPassword") === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(
                    new Error("Mật khẩu xác nhận không khớp!")
                  );
                },
              }),
            ]}
          >
            <div>
              <label className="block text-gray-300 mb-1">
                Xác nhận mật khẩu
              </label>
              <Input.Password
                className="w-full px-4 py-2 rounded-md bg-gray-800 border border-gray-600 text-white focus:outline-none focus:border-green-500 focus:bg-gray-700 transition-colors duration-200 custom-input custom-password-input"
                placeholder="Xác nhận mật khẩu"
              />
            </div>
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              className="w-full bg-green-500 text-white py-2 rounded-lg hover:bg-green-600 transition"
              loading={loading}
            >
              Đặt lại mật khẩu
            </Button>
          </Form.Item>
        </Form>
      </div>

      <style jsx>{`
        .custom-input {
          background-color: #1f2937 !important; /* gray-800 */
          color: #ffffff !important; /* white */
          border: 1px solid #4b5563 !important; /* gray-600 */
          border-radius: 6px !important;
        }
        .custom-input:hover {
          background-color: #ffffff !important; /* white */
        }
        .custom-input:focus {
          background-color: #374151 !important; /* gray-700 */
          border-color: #10b981 !important; /* green-500 */
        }
        .custom-input::placeholder {
          color: #ffffff !important; /* white */
        }
        .custom-input:hover::placeholder {
          color: #000000 !important; /* black */
        }

        .custom-password-input .ant-input-affix-wrapper {
          background-color: #1f2937 !important; /* gray-800 */
          border: 1px solid #4b5563 !important; /* gray-600 */
          border-radius: 6px !important;
        }
        .custom-password-input .ant-input-affix-wrapper:hover {
          background-color: #ffffff !important; /* white */
        }
        .custom-password-input .ant-input-affix-wrapper:focus-within {
          background-color: #374151 !important; /* gray-700 */
          border-color: #10b981 !important; /* green-500 */
        }
        .custom-password-input .ant-input {
          background-color: transparent !important; /* Make input background transparent to match wrapper */
          color: #ffffff !important; /* white */
          border: none !important; /* Remove inner input border */
        }
        .custom-password-input .ant-input:hover {
          background-color: transparent !important; /* Keep transparent on hover */
        }
        .custom-password-input .ant-input:focus {
          background-color: transparent !important; /* Keep transparent on focus */
        }
        .custom-password-input .ant-input::placeholder {
          color: #ffffff !important; /* white */
        }
        .custom-password-input .ant-input:hover::placeholder {
          color: #000000 !important; /* black */
        }
        .custom-password-input .ant-input-password-icon {
          color: #ffffff !important; /* white */
        }
        .custom-password-input .ant-input-password-icon:hover {
          color: #10b981 !important; /* green-500 */
        }
        .custom-password-input .ant-input-suffix {
          background: transparent !important; /* Remove the line by making suffix background transparent */
        }
        .custom-password-input .ant-input-affix-wrapper {
          padding-right: 12px !important; /* Adjust padding to align icon properly */
        }
      `}</style>
    </div>
  );
};

export default ResetPassword;
