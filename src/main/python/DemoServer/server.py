import datetime
import json
import random
import socket
import sys
import torch
import cv2
import numpy as np
import select
import threading
import os
import time

from concurrent.futures import ThreadPoolExecutor

from scipy.optimize import linear_sum_assignment

from DetModel.DetectModel import DetectModel
from log import run_log as logger

from BackEndTools.demo import ImageDemoSample
from IPOSE.demo import ImageDemoSampleClassI

PING = "ping"

UploadImageStoreRootPath = "sources/username/uploadImage"
ResultImageStoreRootPath = "sources/username/resImage"

detmodel = DetectModel("/home/caw/public/DemoServer/weights/best.pt")

ImageProcess = ImageDemoSample("BackEndTools\\configs\\demoConfigs.yaml".replace("\\", "/"))
ImageDemoClassI = ImageDemoSampleClassI("IPOSE/configs/demoConfigs.yaml")

host = "127.0.0.1"
port = 8701


def matching(bboxes, keypoints):
    keypoints = torch.as_tensor(keypoints)
    centers = torch.as_tensor(
        [0.75 * bbox[:2] + 0.25 * bbox[2:] for bbox in bboxes]
    ).double()
    print(centers.shape, keypoints.shape)
    cost = torch.cdist(torch.as_tensor(centers),
                       torch.as_tensor(keypoints).double())
    # print(cost.shape)

    row_index, col_index = linear_sum_assignment(cost)
    return row_index, col_index


def plot_one_box(x, img, color=None, label=None, line_thickness=3):
    # Plots one bounding box on image img
    tl = line_thickness or round(0.002 * (img.shape[0] + img.shape[1]) / 2) + 1  # line/font thickness
    color = color or [random.randint(0, 255) for _ in range(3)]
    c1, c2 = (int(x[0]), int(x[1])), (int(x[2]), int(x[3]))
    cv2.rectangle(img, c1, c2, color, thickness=tl, lineType=cv2.LINE_AA)
    tf = max(tl - 1, 1)  # font thickness
    t_size = cv2.getTextSize(label, 0, fontScale=tl / 3, thickness=tf)[0]
    c2 = c1[0] + t_size[0], c1[1] - t_size[1] - 3
    cv2.rectangle(img, c1, c2, color, -1, cv2.LINE_AA)  # filled
    cv2.putText(img, label, (c1[0], c1[1] - 2), 0, tl, [225, 255, 255], thickness=tf, lineType=cv2.LINE_AA)


def server():
    serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    serversocket.bind((host, port))
    serversocket.listen()
    # serversocket.settimeout(-1)
    logger.info(f"server started, listen on {host}:{port}")
    while True:
        clientsocket, addr = serversocket.accept()
        logger.info(f"tcp established from {addr}")
        try:
            thread = ServerThread(clientsocket)
            thread.start()
        except Exception as identifier:
            logger.error(identifier)
    # serversocket.close()


class ServerThread(threading.Thread):
    def __init__(self, clientsocket: socket.socket, encoding='utf-8'):
        threading.Thread.__init__(self)
        self._socket = clientsocket
        self._encoding = encoding

    def run(self):
        textio = self._socket.makefile(encoding=self._encoding)
        msg = textio.readline().rstrip()
        while msg != '':
            if msg == PING:
                continue
            res: str = handleMsg(msg)
            if res != '':
                res += "\n"
                self._socket.send(res.encode())
            msg = textio.readline().rstrip()
        textio.close()


def handleMsg(msg):

    upload_image_path = os.path.join(UploadImageStoreRootPath, msg)
    logger.info(f"原始图片路径：{upload_image_path}")
    td = time.strftime("%Y%m%d", time.localtime())
    ResultImageStorePath = os.path.join(ResultImageStoreRootPath, td)
    if not os.path.exists(ResultImageStorePath):
        os.makedirs(ResultImageStorePath)

    ResultImageStorePath = os.path.join(ResultImageStoreRootPath, msg)
    # 开始处理

    bboxes, labels = detmodel(upload_image_path)
    bboxes = [np.array(bbox, dtype=np.float32) for bbox in bboxes]
    l = len(list(bboxes))
    code = 0
    points = []
    if l == 16 or l == 11:
        if l == 16:
            Pose = ImageProcess(upload_image_path, ResultImageStorePath)
        else:
            Pose = ImageDemoClassI(upload_image_path, ResultImageStorePath)
        logger.info(f"检测点个数{len(list(bboxes))} 姿态检测个数{len(list(Pose))}")
        if l == len(list(Pose)):
            _, order = matching(bboxes, Pose[:, :2])
            code = 1
            points = [str(labels[order[i]])[2:][:-5] for i in range(l)]
            img = cv2.imread(upload_image_path)
            for i in range(l):
                plot_one_box(bboxes[i], img, label=str(order[i] + 1))
            cv2.imwrite(ResultImageStorePath, img)
    # code = 1
    # labels = ["caps" for i in range(16)]
    data = {
        "code": code,
        "imageUri": msg,
        "points": points
    }
    msg = json.dumps(data)
    logger.info(f"算法模型结果：{msg} ")
    return msg



def handlePackage(target_socket, msg, epoll, textio, buffers):
    if PING != msg and '' != msg:
        res = handleMsg(msg)
        if res != '':
            buffers[target_socket] = res + "\n"
            epoll.modify(target_socket, select.EPOLLOUT)
    textio.close()


def epoll_server():
    procPool = ThreadPoolExecutor(15)
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((host, port))
    s.listen()
    s.setblocking(False)
    epoll = select.epoll()
    epoll.register(s.fileno(), select.EPOLLIN | select.EPOLLOUT | select.EPOLLHUP)
    connections = {}
    addresses = {}
    buffers = {}
    logger.info(f"epoll_server started, listen on {host}:{port}")
    while True:
        epoll_list = epoll.poll()
        for fd, events in epoll_list:
            if fd == s.fileno():
                new_socket, new_addr = s.accept()
                connections[new_socket.fileno()] = new_socket
                addresses[new_socket.fileno()] = new_addr
                epoll.register(new_socket.fileno(), select.EPOLLIN | select.EPOLLET)
                logger.info(f"tcp established from {new_addr}, there are {len(connections)} connections now")
            elif events & select.EPOLLIN:
                textio = connections[fd].makefile(encoding='utf-8')
                msg = textio.readline().rstrip()
                if msg != '':
                    procPool.submit(handlePackage, connections[fd], msg, epoll, textio, buffers)
            elif events & select.EPOLLOUT:
                if buffers[fd] != '':
                    connections[fd].send(buffers[fd].encode())
                    buffers[fd] = ''
                epoll.modify(fd, select.EPOLLIN)
            elif events & select.EPOLLHUP:
                epoll.unregister(fd)
                connections[fd].close()
                logger.info(f"{addresses[fd]}---offline---")
                del connections[fd]
                del addresses[fd]


if __name__ == '__main__':
    if len(sys.argv) == 2 and 'epoll' == sys.argv[1]:
        epoll_server()
    else:
        server()
