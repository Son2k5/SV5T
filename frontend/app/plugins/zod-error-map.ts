import { z } from 'zod'

export default defineNuxtPlugin(() => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const customErrorMap = (issue: any) => {
    let message = issue.message || 'Dữ liệu không hợp lệ.'

    switch (issue.code) {
      case 'invalid_type':
        if (issue.input === undefined) {
          message = 'Trường này là bắt buộc.'
        }
        else if (issue.expected === 'date') {
          message = 'Ngày tháng không hợp lệ.'
        }
        else {
          message = `Kiểu dữ liệu không hợp lệ, yêu cầu ${issue.expected}.`
        }
        break
      case 'invalid_value':
        message = 'Giá trị không hợp lệ.'
        break
      case 'unrecognized_keys':
        message = `Chứa khóa không được nhận dạng: ${issue.keys.join(', ')}.`
        break
      case 'invalid_union':
        message = 'Dữ liệu đầu vào không khớp với các lựa chọn cho phép.'
        break
      case 'invalid_format':
        if (issue.format === 'email') {
          message = 'Địa chỉ email không hợp lệ.'
        }
        else if (issue.format === 'url') {
          message = 'Địa chỉ URL không hợp lệ.'
        }
        else if (issue.format === 'uuid') {
          message = 'Mã định danh UUID không hợp lệ.'
        }
        else if (issue.format === 'regex') {
          message = 'Định dạng dữ liệu không hợp lệ.'
        }
        else {
          message = 'Định dạng chuỗi không hợp lệ.'
        }
        break
      case 'too_small':
        if (issue.origin === 'array') {
          message = `Danh sách phải có ít nhất ${issue.minimum} phần tử.`
        }
        else if (issue.origin === 'string') {
          message = `Độ dài tối thiểu là ${issue.minimum} ký tự.`
        }
        else {
          message = `Giá trị phải lớn hơn hoặc bằng ${issue.minimum}.`
        }
        break
      case 'too_big':
        if (issue.origin === 'array') {
          message = `Danh sách chỉ được chứa tối đa ${issue.maximum} phần tử.`
        }
        else if (issue.origin === 'string') {
          message = `Độ dài tối đa là ${issue.maximum} ký tự.`
        }
        else {
          message = `Giá trị phải nhỏ hơn hoặc bằng ${issue.maximum}.`
        }
        break
      case 'custom':
        message = issue.message || 'Dữ liệu không hợp lệ.'
        break
    }

    return { message }
  }

  // Set the error map globally in Zod
  z.setErrorMap(customErrorMap)
})
