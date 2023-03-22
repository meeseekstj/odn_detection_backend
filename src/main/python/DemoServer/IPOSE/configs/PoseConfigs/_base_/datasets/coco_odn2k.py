dataset_info = dict(
    dataset_name='coco_odn',
    paper_info=dict(
        author='Lin, Tsung-Yi and Maire, Michael and '
        'Belongie, Serge and Hays, James and '
        'Perona, Pietro and Ramanan, Deva and '
        r'Doll{\'a}r, Piotr and Zitnick, C Lawrence',
        title='Microsoft coco: Common objects in context',
        container='European conference on computer vision',
        year='2014',
        homepage='http://cocodataset.org/',
    ),
    keypoint_info={
        # 0:
        # dict(name='1k1', id=0, color=[51, 153, 255], type='upper', swap=''),
        # 1:
        # dict(name='1k2',id=1,color=[51, 153, 255],type='upper',swap=''),
        # 2:
        # dict(name='1k3',id=2,color=[51, 153, 255],type='upper',swap=''),
        # 3:
        # dict(name='1k4',id=3,color=[51, 153, 255],type='upper',swap=''),
        # 4:
        # dict(name='1k5',id=4,color=[51, 153, 255],type='upper',swap=''),
        # 5:
        # dict(name='1k6',id=5,color=[0, 255, 0],type='upper',swap=''),
        # 6:
        # dict(name='1k7',id=6,color=[255, 128, 0],type='upper',swap=''),
        # 7:
        # dict(name='1k8',id=7,color=[0, 255, 0],type='upper',swap=''),
        # 8:
        # dict(name='1k9',id=8,color=[255, 128, 0],type='upper',swap=''),
        # 9:
        # dict(name='1k10',id=9,color=[0, 255, 0],type='upper',swap=''),
        # 10:
        # dict(name='1k11',id=10,color=[255, 128, 0],type='upper',swap=''),
        # 11:
        # dict(name='1k12',id=11,color=[0, 255, 0],type='upper',swap=''),
        # 12:
        # dict(name='1k13',id=12,color=[255, 128, 0],type='upper',swap=''),
        # 13:
        # dict(name='1k14',id=13,color=[0, 255, 0],type='upper',swap=''),
        # 14:
        # dict(name='1k15',id=14,color=[255, 128, 0],type='upper',swap=''),
        # 15:
        # dict(name='1k16',id=15,color=[0, 255, 0],type='upper',swap=''),
        # 16:
        # dict(name='1k17',id=16,color=[255, 128, 0],type='upper',swap=''),
        # 17:
        # dict(name='1k18',id=17,color=[255, 128, 0],type='upper',swap=''),
        # 18:
        # dict(name='1k19',id=18,color=[255, 128, 0],type='upper',swap=''),
        # 19:
        # dict(name='1k20',id=19,color=[255, 128, 0],type='upper',swap=''),
        0:
        dict(name='2k1',id=0,color=[255, 128, 0],type='upper',swap=''),
        1:
        dict(name='2k2',id=1,color=[255, 128, 0],type='upper',swap=''),
        2:
        dict(name='2k3',id=2,color=[255, 128, 0],type='upper',swap=''),
        3:
        dict(name='2k4',id=3,color=[255, 128, 0],type='upper',swap=''),
        4:
        dict(name='2k5',id=4,color=[255, 128, 0],type='upper',swap=''),
        5:
        dict(name='2k6',id=5,color=[255, 128, 0],type='upper',swap=''),
        6:
        dict(name='2k7',id=6,color=[255, 128, 0],type='upper',swap=''),
        7:
        dict(name='2k8',id=7,color=[255, 128, 0],type='upper',swap=''),
        8:
        dict(name='2k9',id=8,color=[255, 128, 0],type='upper',swap=''),
        9:
        dict(name='2k10',id=9,color=[255, 128, 0],type='upper',swap=''),
        10:
        dict(name='2k11',id=10,color=[255, 128, 0],type='upper',swap=''),
        11:
        dict(name='2k12',id=11,color=[255, 128, 0],type='upper',swap=''),
        12:
        dict(name='2k13',id=12,color=[255, 128, 0],type='upper',swap='')

    },
    skeleton_info={
        # 0:
        # dict(link=('1k1', '1k2'), id=0, color=[0, 255, 0]),
        # 1:
        # dict(link=('1k2', '1k3'), id=1, color=[0, 255, 0]),
        # 2:
        # dict(link=('1k3', '1k4'), id=2, color=[255, 128, 0]),
        # 3:
        # dict(link=('1k4', '1k5'), id=3, color=[255, 128, 0]),
        # 4:
        # dict(link=('1k5', '1k6'), id=4, color=[51, 153, 255]),
        # 5:
        # dict(link=('1k6', '1k7'), id=5, color=[51, 153, 255]),
        # 6:
        # dict(link=('1k7', '1k8'), id=6, color=[51, 153, 255]),
        # 7:
        # dict(link=('1k8', '1k9'),id=7,color=[51, 153, 255]),
        # 8:
        # dict(link=('1k9', '1k10'), id=8, color=[0, 255, 0]),
        # 9:
        # dict(link=('1k10', '1k11'), id=9, color=[255, 128, 0]),
        # 10:
        # dict(link=('1k11', '1k12'), id=10, color=[0, 255, 0]),
        # 11:
        # dict(link=('1k12', '1k13'), id=11, color=[255, 128, 0]),
        # 12:
        # dict(link=('1k13', '1k14'), id=12, color=[51, 153, 255]),
        # 13:
        # dict(link=('1k14', '1k15'), id=13, color=[51, 153, 255]),
        # 14:
        # dict(link=('1k15', '1k16'), id=14, color=[51, 153, 255]),
        # 15:
        # dict(link=('1k16', '1k17'), id=15, color=[51, 153, 255]),
        # 16:
        # dict(link=('1k17', '1k18'), id=16, color=[51, 153, 255]),
        # 17:
        # dict(link=('1k18', '1k19'), id=17, color=[51, 153, 255]),
        # 18:
        # dict(link=('1k19', '1k20'), id=18, color=[51, 153, 255]),
        0:
        dict(link=('2k1', '2k2'), id=0, color=[51, 153, 255]),
        1:
        dict(link=('2k2', '2k3'), id=1, color=[51, 153, 255]),
        2:
        dict(link=('2k3', '2k4'), id=2, color=[51, 153, 255]),
        3:
        dict(link=('2k4', '2k5'), id=3, color=[51, 153, 255]),
        4:
        dict(link=('2k5', '2k6'), id=4, color=[51, 153, 255]),
        5:
        dict(link=('2k6', '2k7'), id=5, color=[51, 153, 255]),
        6:
        dict(link=('2k7', '2k8'), id=6, color=[51, 153, 255]),
        7:
        dict(link=('2k8', '2k9'), id=7, color=[51, 153, 255]),
        8:
        dict(link=('2k9', '2k10'), id=8, color=[51, 153, 255]),
        9:
        dict(link=('2k10', '2k11'), id=9, color=[51, 153, 255]),
        10:
        dict(link=('2k11', '2k12'), id=10, color=[51, 153, 255]),
        11:
        dict(link=('2k12', '2k13'), id=11, color=[51, 153, 255])
    },
    joint_weights=[
        1., 1., 1., 1., 1., 1., 1., 1.2, 1.2, 1.5, 1.5, 1.,1.
        # 1.5,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.
    ],
    sigmas=[
        0.026, 0.025, 0.025, 0.035, 0.035, 0.079, 0.079, 0.072, 0.072, 0.062,
        0.062, 0.107, 0.107
        #  0.107, 0.087, 0.087, 0.089, 0.089, 0.055, 0.022, 0.072,
        # 0.087, 0.087, 0.087, 0.087, 0.087, 0.087, 0.087, 0.087, 0.087, 0.087,
        # 0.087, 0.087, 0.087
    ]
    )
